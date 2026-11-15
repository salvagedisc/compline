#version 150

// Compline : la passe finale. Elle pose sur l'image du jeu, dans cet ordre :
//  1. l'air : un voile chaud du côté du soleil et un peu de fraîcheur à l'opposé, qui grandit
//     avec la distance (la brume d'ATMOSPHERICS reste telle quelle en dessous) ;
//  2. le soleil : l'ombre des arbres, des reliefs et des murs, et des faces tournées vers la
//     lumière plus claires que les autres. Le post-effet ne voit que l'écran : c'est le mod
//     compline (module relief) qui lui donne la hauteur du monde, dans la texture Relief ;
//     puis le ciel qui se reflète sur l'eau, et une brume basse qui dort dans les creux ;
//  3. les rayons de soleil et le halo des lumières, calculés par les passes précédentes ;
//     sous l'eau, cette étape laisse la place à l'eau elle-même : une eau qui boit les couleurs
//     selon sa teinte (celle du biome), s'assombrit avec la profondeur, porte des grains en
//     suspension, et laisse voir le ciel par un cercle à la surface, entouré d'un miroir ;
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
uniform float ForceEau;
uniform float ForceReflets;
uniform float ForceBrume;
uniform mat4 PolyProjMat;
uniform mat4 PolyModelViewMat;

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

vec4 hasard4(vec3 p) {
    vec4 q = fract(vec4(p.xyzx) * vec4(0.1031, 0.1030, 0.0973, 0.1099));
    q += dot(q, q.wzxy + 33.33);
    return fract((q.xxyz + q.yzzw) * q.zywx);
}

// la surface de l'eau dans une colonne (y de monde), -4096 si la colonne n'est pas de l'eau
float surfaceEau(vec2 xz, float bas) {
    vec4 t = texelFetch(Relief, ivec2(floor(xz)) & 255, 0);
    if (abs(t.a * 255.0 - 200.0) > 0.5 || abs(t.b - 0.5) > 0.1) {
        return -4096.0;
    }
    // le haut du dernier bloc d'eau, moins le creux d'une eau de source
    return floor(t.r * 255.0 + 0.5) + floor(t.g * 255.0 + 0.5) * 256.0 + bas - 0.11;
}

// un ciel estimé d'après l'heure et la pluie, quand l'image ne montre pas le vrai
vec3 cielEstime(vec3 dir, float jour, float nuit) {
    float h = sqrt(clamp(dir.y, 0.0, 1.0));
    vec3 c = mix(vec3(0.60, 0.66, 0.68), vec3(0.30, 0.42, 0.58), h) * jour + vec3(0.008, 0.011, 0.024) * nuit;
    return c * mix(1.0, 0.55, Pluie);
}

// le ciel dans une direction : l'image elle-même quand cette direction tombe à l'écran sur du ciel
// (on garde alors la brume, le couchant et les nuages du jeu), sinon l'estimation
vec3 cielVers(vec3 dir, vec3 secours) {
    vec4 clip = PolyProjMat * (PolyModelViewMat * vec4(dir, 0.0));
    if (clip.w > 1.0e-4) {
        vec2 uv = clip.xy / clip.w * 0.5 + 0.5;
        if (all(greaterThan(uv, vec2(0.002))) && all(lessThan(uv, vec2(0.998)))
                && texture(InDepth, uv).r >= 0.99999) {
            return pow(texture(DiffuseSampler, uv).rgb, vec3(2.2));
        }
    }
    return secours;
}

// les grains en suspension dans l'eau : des cellules d'un bloc et demi, une sur deux porte un grain
// qui descend lentement ; on les compte le long du regard
float neigeMarine(vec3 regard, float jusque, float alea) {
    vec3 oeil = vec3(CamX, CamY, CamZ);
    float total = 0.0;
    float t = 0.5 + alea * 0.8;
    for (int i = 0; i < 18; ++i) {
        if (t > jusque) {
            break;
        }
        vec3 cellule = floor((oeil + regard * t) / 1.5);
        vec4 h = hasard4(cellule);
        if (h.w > 0.5) {
            vec3 grain = (cellule + vec3(h.x, fract(h.y - Temps * 0.0005 * (0.5 + h.z)), h.z)) * 1.5;
            grain.xz += 0.2 * sin(Temps * 0.015 + h.wx * 6.2831);
            vec3 vers = grain - oeil;
            float loin = dot(vers, regard);
            // chaque grain n'est compté qu'une fois, au pas qui le croise
            if (abs(loin - t) < 0.4) {
                float ecart = length(vers - regard * loin);
                float taille = 0.016 + 0.0022 * loin;
                total += smoothstep(taille, taille * 0.25, ecart) * exp(-loin * 0.10);
            }
        }
        t += 0.8;
    }
    return total;
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
    float bas = floor(entete.g * 255.0 + 0.5) - 128.0;
    vec3 oeil = vec3(CamX, CamY, CamZ);

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
        float jourRecu = ensoleillement(monde, versSoleil, bas, bruit(gl_FragCoord.xy));
        // une face qui tourne le dos au soleil reste dans son ombre propre
        jourRecu *= mix(1.0, smoothstep(-0.15, 0.30, dot(normale, versSoleil)), 0.65);
        vec3 teinteOmbre = mix(vec3(0.50, 0.58, 0.72), vec3(0.58, 0.54, 0.62), crepuscule);
        vec3 teinteJour = mix(vec3(1.20, 1.13, 0.98), vec3(1.27, 1.07, 0.85), crepuscule);
        lin *= mix(vec3(1.0), mix(teinteOmbre, teinteJour, jourRecu), min(soleil, 1.0));
    }

    // le ciel sur l'eau : là où le regard traverse une surface d'eau (le mod dit quelles colonnes
    // en portent), l'eau reflète le ciel, peu quand on la regarde d'en haut, beaucoup en rasant
    if (connu && sousEau < 0.5 && !ciel && ForceReflets > 0.01 && regard.y < -0.01 && eloignement < 160.0) {
        vec3 monde = oeil + ici;
        float s = surfaceEau(monde.xz, bas);
        if (s > -4000.0 && monde.y <= s + 0.08 && CamY > s) {
            float tS = (CamY - s) / -regard.y;
            vec3 touche = oeil + regard * tS;
            if (tS <= eloignement + 0.1 && abs(surfaceEau(touche.xz, bas) - s) < 0.5) {
                // de petites rides, plus vives sous la pluie
                vec2 w = touche.xz;
                vec2 pente = vec2(sin(w.x * 1.7 + Temps * 0.11 + sin(w.y * 0.9)) + 0.6 * sin(w.y * 3.1 - Temps * 0.17),
                                  cos(w.y * 1.5 + Temps * 0.09 + sin(w.x * 1.1)) + 0.6 * cos(w.x * 2.9 + Temps * 0.13));
                vec3 n = normalize(vec3(pente.x * 0.022 * (1.0 + 2.5 * Pluie), 1.0, pente.y * 0.022 * (1.0 + 2.5 * Pluie)));
                vec3 r = reflect(regard, n);
                r.y = abs(r.y);
                float cosI = max(dot(-regard, n), 0.0);
                float fresnel = 0.02 + 0.98 * pow(1.0 - cosI, 5.0);
                vec3 vu = cielVers(r, cielEstime(r, jour, nuit) * (0.35 + 0.65 * dehors));
                float part = min(fresnel * ForceReflets, 0.75) * smoothstep(160.0, 90.0, tS);
                lin = mix(lin, vu, part);
                // l'éclat du soleil sur les rides
                float eclat = pow(max(dot(r, versSoleil), 0.0), 350.0) * smoothstep(0.02, 0.12, hauteur);
                lin += teinteAstre * eclat * 5.0 * sec * dehors * entete.r * ForceReflets;
            }
        }
    }

    // la brume basse : plus épaisse au ras de l'eau et dans les creux (sous 64 + quelques blocs),
    // surtout à l'aube, au crépuscule et sous la pluie
    // (elle ne peint que le terrain : le ciel garde la brume d'ATMOSPHERICS)
    if (ForceBrume > 0.01 && dehors > 0.01 && !ciel) {
        float base = 63.0;
        float echelle = 6.0;
        float densite = 0.010 * (0.30 + 0.90 * crepuscule * jour + 0.70 * Pluie);
        float trajet = min(eloignement, 240.0);
        float yFin = CamY + regard.y * trajet;
        // sous la base, la brume a partout la même épaisseur
        float a = clamp((CamY - base) / echelle, 0.0, 40.0);
        float b = clamp((yFin - base) / echelle, 0.0, 40.0);
        float quantite = abs(b - a) > 1.0e-3
            ? densite * trajet * (exp(-min(a, b)) - exp(-max(a, b))) / abs(b - a)
            : densite * trajet * exp(-a);
        float brume = (1.0 - exp(-min(quantite, 1.2))) * min(ForceBrume, 1.0) * min(dehors, 1.0) * 0.8;
        vec3 teinteBrume = (vec3(0.42, 0.46, 0.45) * jour + vec3(0.014, 0.018, 0.030) * nuit) * mix(1.0, 0.72, Pluie)
                         + teinteAstre * lobe * 0.25;
        lin = mix(lin, teinteBrume, brume);
    }

    if (sousEau > 0.5) {
        // la teinte de l'eau du biome (donnée par le mod), ramenée à une même clarté et tirée vers le
        // vert d'étang du pack ; elle décide de ce que l'eau boit : la couleur dominante porte loin
        vec4 eauTexel = texelFetch(Relief, ivec2(1, 0), 0);
        vec4 surfTexel = texelFetch(Relief, ivec2(2, 0), 0);
        bool eauConnue = connu && abs(eauTexel.a * 255.0 - 203.0) < 0.5;
        vec3 teinte = eauConnue ? eauTexel.rgb : vec3(0.02, 0.12, 0.14);
        teinte /= max(max(teinte.r, teinte.g), max(teinte.b, 1.0e-3));
        // le bleu vif du jeu passe mal avec le reste du pack : la teinte garde son sens (eau de mer,
        // de marais, de rivière) mais tire vers un vert d'étang et perd de sa vivacité
        teinte = mix(teinte, vec3(0.62, 1.00, 0.66), 0.65);
        teinte = mix(vec3(dot(teinte, vec3(0.30, 0.55, 0.15))), teinte, 0.5);
        vec3 teinteLin = pow(teinte, vec3(2.2));
        vec3 extinction = mix(vec3(0.28), vec3(0.07), teinte) * mix(1.0, 1.5, Pluie);
        float audessus = eauConnue && abs(surfTexel.a * 255.0 - 203.0) < 0.5 ? surfTexel.r * 255.0 / 4.0 : 6.0;
        bool airLibre = eauConnue && surfTexel.g > 0.5;
        float surface = CamY + audessus;
        float clarte = (airLibre ? 1.0 : Ciel) * max(jour, 0.12 * nuit);
        float force = min(ForceEau, 1.0);
        vec3 avantEau = lin;

        // le jeu teinte déjà ce qu'on voit sous l'eau de son bleu : on le ramène à la teinte de l'eau
        lin = mix(lin, vec3(lum(lin)) * teinteLin / max(lum(teinteLin), 1.0e-3), 0.55);
        // ce qui est loin sous la surface reçoit moins de jour (le jeu l'assombrit déjà un peu)
        float dP = max(surface - (CamY + ici.y), 0.0);
        lin *= mix(vec3(1.0), exp(-extinction * dP * 0.6), 0.5);
        // reflets mouvants sur ce qui regarde vers le haut, d'autant moins que c'est profond
        if (!ciel && eloignement < 40.0) {
            vec3 normale = faceVue(ici);
            vec3 monde = oeil + ici;
            float motif = reflets(monde.xz * 0.85 + monde.y * 0.3, Temps) * 0.6
                        + reflets(monde.xz * 0.47 - Temps * 0.004, Temps * 0.7) * 0.4;
            lin *= 1.0 + 0.9 * motif * smoothstep(0.2, 0.8, normale.y) * clarte * exp(-eloignement / 16.0) * exp(-dP * 0.12);
        }

        // le trajet jusqu'à l'oeil : la scène s'éteint couleur par couleur, l'eau éclairée la remplace
        float dist = ciel ? 64.0 : min(eloignement, 64.0);
        vec3 passe = exp(-extinction * dist);
        float dMilieu = max(surface - (CamY + regard.y * dist * 0.5), 0.0);
        float avant = pow(max(regard.y, 0.0), 3.0);
        vec3 eclaire = teinteLin * 0.13 * clarte * exp(-extinction * dMilieu * 0.7) * (1.0 + 2.2 * avant * jour);
        // faisceaux qui tombent de la surface, plus nets quand on lève les yeux
        vec3 cote = normalize(cross(versSoleil, vec3(0.0, 0.0, 1.0)) + vec3(0.0, 0.0, 0.001));
        vec2 plan = vec2(dot(regard, cote), regard.z) / max(0.25, abs(dot(regard, versSoleil)) + 0.35);
        float faisceaux = sin(plan.x * 9.0 + sin(plan.y * 4.0 + Temps * 0.03) * 1.5 + Temps * 0.012)
                        * sin(plan.y * 7.0 - Temps * 0.017 + plan.x * 2.0);
        faisceaux = smoothstep(0.25, 0.95, faisceaux * 0.5 + 0.5);
        eclaire += teinteLin * faisceaux * smoothstep(0.1, 1.0, regard.y * 0.5 + 0.5) * 0.05 * jour * clarte * exp(-audessus * 0.08);
        lin = lin * passe + eclaire * (1.0 - passe);

        // vue d'en dessous, la surface montre le ciel dans un cercle (au-delà de 48 degrés de la
        // verticale, elle devient un miroir qui renvoie le fond)
        if (airLibre && regard.y > 0.02) {
            float tPlan = audessus / regard.y;
            if (ciel || tPlan < eloignement + 0.4) {
                vec3 touche = oeil + regard * tPlan;
                vec2 ride = vec2(sin(touche.x * 1.3 + Temps * 0.10 + sin(touche.z * 0.7)),
                                 cos(touche.z * 1.1 + Temps * 0.08 + sin(touche.x * 0.9))) * 0.035;
                vec3 r = normalize(regard + vec3(ride.x, 0.0, ride.y));
                float fenetre = smoothstep(0.58, 0.74, r.y);
                vec3 sortie = refract(r, vec3(0.0, -1.0, 0.0), 1.333);
                // quand le point vu est la surface elle-même, le jeu n'y montre que son bleu : on y met le
                // ciel ; un objet au-delà de la surface (un arbre de la rive) reste visible
                bool auDela = !ciel && eloignement > tPlan + 1.0;
                vec3 dehorsVu = auDela ? vec3(lum(avantEau)) * vec3(0.92, 1.00, 0.96) : cielEstime(sortie, jour, nuit) * 0.8;
                dehorsVu += teinteAstre * pow(max(dot(sortie, versSoleil), 0.0), 220.0) * 1.5 * jour;
                vec3 passePlan = exp(-extinction * tPlan);
                vec3 transmis = dehorsVu * passePlan + eclaire * (1.0 - passePlan);
                vec3 miroir = teinteLin * 0.05 * clarte + eclaire * 0.6;
                vec3 vuSurface = mix(miroir, transmis, fenetre);
                lin = mix(lin, vuSurface, 0.6 * smoothstep(0.02, 0.12, regard.y));
            }
        }

        // les grains en suspension, éclairés surtout quand on regarde vers la lumière
        float grains = neigeMarine(regard, min(dist, 20.0), bruit(gl_FragCoord.xy + 7.0));
        lin += vec3(0.80, 0.92, 0.82) * grains * clarte * (0.06 + 0.35 * avant * jour);

        lin = mix(avantEau, lin, force);
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
