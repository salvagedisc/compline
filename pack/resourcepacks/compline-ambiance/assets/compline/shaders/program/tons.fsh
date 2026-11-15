#version 150

// Compline : la passe finale. Elle pose sur l'image du jeu, dans cet ordre :
//  1. l'air : un voile chaud du côté du soleil et un peu de fraîcheur à l'opposé, qui grandit
//     avec la distance (la brume d'ATMOSPHERICS reste telle quelle en dessous) ;
//  2. le soleil : l'ombre des arbres, des reliefs et des murs, et des faces tournées vers la
//     lumière plus claires que les autres. Le post-effet ne voit que l'écran : c'est le mod
//     compline (module relief) qui lui donne la hauteur du monde, dans la texture Relief ;
//  3. les rayons de soleil et le halo des lumières, calculés par les passes précédentes ;
//     sous l'eau, cette étape laisse la place à une eau trouble, à des faisceaux qui tombent de
//     la surface et à des reflets mouvants sur le fond ;
//  4. les tons : verts tirés vers l'olive, ombres froides et lumières chaudes, nuits bleutées,
//     crépuscules dorés, pluie plus grise, un contraste un peu plus doux.
// Tout se règle dans l'écran de Polytone (le pinceau de l'écran des packs de ressources).

uniform sampler2D DiffuseSampler;
uniform sampler2D InDepth;
uniform sampler2D RayonsSampler;
uniform sampler2D HaloSampler;
uniform sampler2D Relief;

in vec2 texCoord;
in vec3 rayon;
flat in vec2 plans;
flat in vec3 versSoleil;

uniform float Pluie;
uniform float Ciel;
uniform float ForceHalo;
uniform float ForceAir;
uniform float ForceTons;
uniform float ForceOlive;
uniform float ForceOmbres;
uniform float CamX;
uniform float CamY;
uniform float CamZ;
uniform float Temps;

out vec4 fragColor;

const vec3 SOLEIL_BAS = vec3(1.00, 0.62, 0.34);
const vec3 SOLEIL_HAUT = vec3(1.00, 0.94, 0.82);
const vec3 LUNE = vec3(0.50, 0.62, 0.90);

float lum(vec3 c) {
    return dot(c, vec3(0.2126, 0.7152, 0.0722));
}

// lecture adoucie d'une petite image (les cibles du jeu se lisent sinon au pixel le plus proche)
vec3 doux(sampler2D s, vec2 uv) {
    vec2 taille = vec2(textureSize(s, 0));
    vec2 p = uv * taille - 0.5;
    ivec2 i = ivec2(floor(p));
    vec2 f = p - vec2(i);
    ivec2 mx = ivec2(taille) - 1;
    vec3 a = texelFetch(s, clamp(i, ivec2(0), mx), 0).rgb;
    vec3 b = texelFetch(s, clamp(i + ivec2(1, 0), ivec2(0), mx), 0).rgb;
    vec3 c = texelFetch(s, clamp(i + ivec2(0, 1), ivec2(0), mx), 0).rgb;
    vec3 d = texelFetch(s, clamp(i + ivec2(1, 1), ivec2(0), mx), 0).rgb;
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float bruit(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

float distanceVue(float profondeur) {
    float q = profondeur * 2.0 - 1.0 + plans.x;
    return q < -1.0e-7 ? min(plans.y / q, 4096.0) : 4096.0;
}

// le point vu à un pixel voisin, dans le repère du monde centré sur la caméra
vec3 lieu(vec2 decalage) {
    vec2 px = decalage / vec2(textureSize(InDepth, 0));
    vec3 r = rayon + dFdx(rayon) * decalage.x + dFdy(rayon) * decalage.y;
    return r * distanceVue(texture(InDepth, texCoord + px).r);
}

// la face vue, déduite de la profondeur : de chaque côté on garde le voisin le plus proche,
// pour ne pas inventer de pente au bord d'un objet
vec3 faceVue(vec3 ici) {
    vec3 droite = lieu(vec2(1.0, 0.0)) - ici, gauche = ici - lieu(vec2(-1.0, 0.0));
    vec3 haut = lieu(vec2(0.0, 1.0)) - ici, bas = ici - lieu(vec2(0.0, -1.0));
    vec3 dx = dot(droite, droite) < dot(gauche, gauche) ? droite : gauche;
    vec3 dy = dot(haut, haut) < dot(bas, bas) ? haut : bas;
    vec3 n = normalize(cross(dx, dy));
    return dot(n, ici) > 0.0 ? -n : n;
}

// hauteur de la surface du monde dans une colonne (y de monde), -4096 si elle est inconnue
float relief(vec2 xz, float bas, out float feuillage) {
    vec4 t = texelFetch(Relief, ivec2(floor(xz)) & 255, 0);
    feuillage = step(0.9, t.b);
    // une colonne inconnue ne cache rien, une colonne d'eau non plus (on voit le fond à travers elle)
    if (abs(t.a * 255.0 - 200.0) > 0.5 || abs(t.b - 0.5) < 0.1) {
        return -4096.0;
    }
    return floor(t.r * 255.0 + 0.5) + floor(t.g * 255.0 + 0.5) * 256.0 + bas;
}

vec2 hasard(vec2 p) {
    vec3 q = fract(vec3(p.xyx) * vec3(0.1031, 0.1030, 0.0973));
    q += dot(q, q.yzx + 33.33);
    return fract((q.xx + q.yz) * q.zy);
}

// taches de soleil sous un feuillage : des trouées de la taille d'un demi-bloc, les mêmes tout le
// long d'un rayon de soleil, qui s'ouvrent et se referment doucement comme des feuilles qui bougent
float trouees(vec3 monde, vec3 astre) {
    vec2 p = monde.xz - astre.xz * (monde.y / max(astre.y, 0.05));
    vec2 c = floor(p * 2.0);
    vec2 h = hasard(c);
    return smoothstep(0.60, 0.74, h.x + 0.10 * sin(Temps * 0.045 + h.y * 6.2831));
}

// part de soleil qui arrive en un point du monde : on avance vers l'astre à travers le relief
float ensoleillement(vec3 monde, vec3 astre, float bas, float alea) {
    float ombre = 0.0;
    float sousFeuilles = 0.0;
    float pas = 0.30;
    float t = pas * (0.4 + alea);
    for (int i = 0; i < 26; ++i) {
        vec3 q = monde + astre * t;
        float feuillage;
        float dessous = relief(q.xz, bas, feuillage) - q.y;
        if (dessous > 0.0) {
            // bord adouci, de plus en plus large avec la distance
            float prise = smoothstep(0.0, 0.12 + t * 0.05, dessous);
            if (feuillage > 0.5 && dessous < 9.0) {
                sousFeuilles = max(sousFeuilles, prise);
            } else {
                ombre = max(ombre, prise);
            }
            if (ombre > 0.98) {
                break;
            }
        }
        if (q.y > CamY + 150.0 || t > 110.0) {
            break;
        }
        pas *= 1.19;
        t += pas;
    }
    // un feuillage laisse passer un peu de jour partout, et beaucoup dans ses trouées
    float feuilles = sousFeuilles * mix(0.88, 0.10, trouees(monde, astre));
    return 1.0 - max(ombre, feuilles);
}

// reflets de l'eau sur le fond : les arêtes d'un pavage de cellules dont les centres dérivent
float reflets(vec2 p, float temps) {
    vec2 c = floor(p), f = p - c;
    float proche = 8.0, suivant = 8.0;
    for (int j = -1; j <= 1; ++j) {
        for (int i = -1; i <= 1; ++i) {
            vec2 g = vec2(float(i), float(j));
            vec2 h = hasard(c + g);
            vec2 centre = g + 0.5 + 0.38 * sin(temps * (0.05 + 0.04 * h.x) + 6.2831 * h);
            float d = dot(centre - f, centre - f);
            if (d < proche) {
                suivant = proche;
                proche = d;
            } else if (d < suivant) {
                suivant = d;
            }
        }
    }
    float arete = sqrt(suivant) - sqrt(proche);
    return smoothstep(0.16, 0.0, arete);
}

void main() {
    // l'en-tête de la texture du mod : sans lui, ni ombres ni rendu sous l'eau
    vec4 entete = texelFetch(Relief, ivec2(0), 0);
    bool connu = textureSize(Relief, 0).x == 256 && abs(entete.a * 255.0 - 201.0) < 0.5;
    float sousEau = connu && entete.b > 0.5 ? 1.0 : 0.0;
    // un pack de shaders (Iris) dessine déjà sa lumière : on lui laisse l'image
    if (textureSize(Relief, 0).x == 256 && abs(entete.a * 255.0 - 202.0) < 0.5) {
        fragColor = vec4(texture(DiffuseSampler, texCoord).rgb, 1.0);
        return;
    }

    vec2 vue = texCoord;
    if (sousEau > 0.5) {
        // l'eau déforme un peu ce qu'on voit à travers elle
        vue += vec2(sin(texCoord.y * 17.0 + Temps * 0.085), cos(texCoord.x * 13.0 + Temps * 0.07)) * 0.0020;
    }
    vec3 image = texture(DiffuseSampler, vue).rgb;
    float profondeur = texture(InDepth, vue).r;
    bool ciel = profondeur >= 0.99999;

    // distance du point vu, en blocs
    vec3 ici = rayon * distanceVue(profondeur);
    float eloignement = ciel ? 4096.0 : length(ici);
    vec3 regard = normalize(rayon);

    float hauteur = versSoleil.y;
    float jour = smoothstep(-0.08, 0.12, hauteur);
    float nuit = smoothstep(0.02, -0.22, hauteur);
    float crepuscule = smoothstep(0.42, 0.02, abs(hauteur));
    float dehors = smoothstep(0.2, 0.75, Ciel) * (1.0 - sousEau);
    float sec = 1.0 - Pluie;

    vec3 lin = pow(image, vec3(2.2));

    // 1. l'air
    vec3 astre = hauteur >= -0.05 ? versSoleil : -versSoleil;
    vec3 teinteAstre = mix(SOLEIL_BAS, SOLEIL_HAUT, smoothstep(0.05, 0.55, hauteur)) * jour + LUNE * nuit * 0.12;
    float face = max(dot(regard, astre), 0.0);
    float lobe = face * face;
    lobe = lobe * lobe * lobe;
    float voile = 1.0 - exp(-eloignement / 150.0);
    float air = ForceAir * dehors * mix(0.35, 1.0, sec);
    lin += teinteAstre * (0.16 * lobe + 0.025 * face) * voile * air;
    lin *= mix(vec3(1.0), vec3(0.93, 0.97, 1.04), voile * (1.0 - face) * 0.6 * min(air, 1.0) * jour);

    // 2. le soleil
    float soleil = ForceOmbres * dehors * sec * smoothstep(0.04, 0.22, hauteur) * entete.r;
    soleil *= smoothstep(112.0, 80.0, eloignement);
    if (connu && !ciel && soleil > 0.01) {
        vec3 normale = faceVue(ici);
        vec3 monde = vec3(CamX, CamY, CamZ) + ici + normale * 0.06;
        float bas = floor(entete.g * 255.0 + 0.5) - 128.0;
        float jourRecu = ensoleillement(monde, versSoleil, bas, bruit(gl_FragCoord.xy));
        // une face qui tourne le dos au soleil reste dans son ombre propre
        jourRecu *= mix(1.0, smoothstep(-0.15, 0.30, dot(normale, versSoleil)), 0.65);
        vec3 teinteOmbre = mix(vec3(0.50, 0.58, 0.72), vec3(0.58, 0.54, 0.62), crepuscule);
        vec3 teinteJour = mix(vec3(1.20, 1.13, 0.98), vec3(1.27, 1.07, 0.85), crepuscule);
        lin *= mix(vec3(1.0), mix(teinteOmbre, teinteJour, jourRecu), min(soleil, 1.0));
    }

    if (sousEau > 0.5) {
        // le bleu vif de l'eau du jeu devient un vert d'étang
        lin = mix(lin, vec3(lum(lin)) * vec3(0.70, 1.00, 0.76), 0.75);
        float clarte = Ciel * max(jour, 0.12 * nuit);
        float haut = regard.y * 0.5 + 0.5;
        // reflets mouvants sur ce qui regarde vers le haut, près de la surface
        if (!ciel && eloignement < 40.0) {
            vec3 normale = faceVue(ici);
            vec3 monde = vec3(CamX, CamY, CamZ) + ici;
            float motif = reflets(monde.xz * 0.85 + monde.y * 0.3, Temps) * 0.6
                        + reflets(monde.xz * 0.47 - Temps * 0.004, Temps * 0.7) * 0.4;
            lin *= 1.0 + 0.9 * motif * smoothstep(0.2, 0.8, normale.y) * clarte * exp(-eloignement / 16.0);
        }
        // faisceaux qui tombent de la surface, plus nets quand on lève les yeux
        vec3 cote = normalize(cross(versSoleil, vec3(0.0, 0.0, 1.0)) + vec3(0.0, 0.0, 0.001));
        vec2 plan = vec2(dot(regard, cote), regard.z) / max(0.25, abs(dot(regard, versSoleil)) + 0.35);
        float faisceaux = sin(plan.x * 9.0 + sin(plan.y * 4.0 + Temps * 0.03) * 1.5 + Temps * 0.012)
                        * sin(plan.y * 7.0 - Temps * 0.017 + plan.x * 2.0);
        faisceaux = smoothstep(0.25, 0.95, faisceaux * 0.5 + 0.5);
        // eau trouble : vert sombre au fond, plus claire vers la surface
        vec3 trouble = mix(vec3(0.014, 0.034, 0.024), vec3(0.095, 0.165, 0.110), smoothstep(0.25, 0.95, haut)) * (0.35 + 0.65 * clarte);
        trouble += vec3(0.20, 0.24, 0.14) * faisceaux * smoothstep(0.35, 1.0, haut) * 0.16 * jour * Ciel;
        float epaisseur = 1.0 - exp(-eloignement / 11.0);
        lin = mix(lin * vec3(0.72, 0.90, 0.80), trouble, epaisseur * 0.88);
    }

    // 3. les rayons et le halo
    vec3 rayons = doux(RayonsSampler, texCoord);
    lin += rayons * rayons * (ciel ? 0.35 : 1.0) * (1.0 - sousEau);
    vec3 halo = doux(HaloSampler, texCoord);
    // (sous l'eau tout l'écran est clair : le halo le ramènerait au bleu du jeu)
    lin += pow(halo, vec3(2.2)) * 0.55 * ForceHalo * (1.0 - sousEau);

    // 4. les tons
    float t = ForceTons;
    // verts olive : les verts dominants perdent du bleu et de la vivacité
    float l = lum(lin);
    float plusFort = max(lin.r, max(lin.g, lin.b));
    float vert = clamp((lin.g - max(lin.r, lin.b)) / max(plusFort, 0.001) * 2.5, 0.0, 1.0);
    vec3 olive = vec3(dot(lin, vec3(0.42, 0.52, 0.06))) * vec3(1.10, 1.00, 0.62);
    lin = mix(lin, olive, vert * 0.42 * ForceOlive);

    // nuits bleutées, crépuscules dorés, pluie grise
    lin *= mix(vec3(1.0), vec3(0.78, 0.92, 1.18), nuit * 0.55 * dehors * t);
    lin *= mix(vec3(1.0), vec3(1.10, 0.98, 0.84), crepuscule * jour * 0.42 * sec * dehors * t);
    l = lum(lin);
    float vivacite = 1.0 - (0.06 + 0.16 * nuit * dehors + 0.14 * Pluie * dehors) * t;
    lin = mix(vec3(l), lin, vivacite);
    // sous la pluie le ciel perd son violet et tire vers un gris un peu vert
    if (ciel) {
        lin = mix(lin, vec3(lum(lin)) * vec3(0.94, 1.00, 0.96), 0.55 * Pluie * dehors * min(t, 1.0));
    }
    // l'orage assombrit le jour
    lin *= 1.0 - 0.22 * Pluie * jour * dehors * t;

    vec3 c = pow(max(lin, vec3(0.0)), vec3(1.0 / 2.2));

    // ombres froides, lumières chaudes
    float clarte = lum(c);
    // (la nuit garde ses ombres telles quelles : elle est déjà sombre)
    float veille = 1.0 - 0.7 * nuit * dehors;
    c += (vec3(-0.012, 0.000, 0.016) * (1.0 - clarte) * (1.0 - clarte) * veille
        + vec3(0.016, 0.006, -0.014) * clarte * clarte) * t;
    // contraste doux
    c = mix(c, c * c * (3.0 - 2.0 * c), 0.22 * t * veille);
    // coins à peine assombris
    vec2 q = texCoord - 0.5;
    c *= 1.0 - 0.14 * smoothstep(0.35, 0.95, dot(q, q) * 2.2) * t;

    c += (bruit(gl_FragCoord.xy) - 0.5) / 255.0;
    fragColor = vec4(clamp(c, 0.0, 1.0), 1.0);
}
