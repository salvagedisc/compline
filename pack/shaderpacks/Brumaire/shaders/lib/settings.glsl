// Brumaire : réglages. Les valeurs entre crochets sont celles que propose le menu d'Iris.

// Brume
#define MIST_DENSITY 1.0 // [0.5 0.75 1.0 1.25 1.5 2.0]
#define MIST_HEIGHT 24.0 // [12.0 16.0 24.0 32.0 48.0]
#define MIST_LIFT 0.7 // [0.0 0.3 0.5 0.7 0.85 1.0]
#define MIST_RING 1.0 // [0.0 0.5 1.0 1.5 2.0]
#define MIST_RING_MAX 512.0 // [256.0 384.0 512.0 768.0 1024.0]

// Lumière
#define SHADOW_FILTER 6 // [1 4 6 9 12]
#define LIGHT_SHAFTS 10 // [0 6 10 16 24]
#define SHAFT_STRENGTH 1.0 // [0.0 0.5 0.75 1.0 1.25 1.5]
#define BLOOM
#define BLOOM_STRENGTH 1.0 // [0.5 0.75 1.0 1.25 1.5]

// Vent et eau
#define WIND_STRENGTH 1.0 // [0.0 0.5 0.75 1.0 1.25 1.5]
#define WATER_WAVES 1.0 // [0.0 0.5 1.0 1.5]

// Couleurs
#define OLIVE 0.6 // [0.0 0.2 0.4 0.6 0.8 1.0]
#define SATURATION 1.0 // [0.7 0.8 0.9 1.0 1.1 1.2]
#define CONTRAST 1.0 // [0.9 0.95 1.0 1.05 1.1 1.15]
#define EXPOSURE 1.0 // [0.7 0.85 1.0 1.15 1.3]

const int shadowMapResolution = 2048; // [1024 1536 2048 3072 4096]
const float shadowDistance = 112.0; // [64.0 80.0 96.0 112.0 128.0 160.0 192.0]
const float sunPathRotation = -28.0; // [-40.0 -28.0 -15.0 0.0 15.0 28.0 40.0]
const bool shadowHardwareFiltering = true;
const float shadowDistanceRenderMul = 1.0;
const float shadowIntervalSize = 2.0;
const float entityShadowDistanceMul = 0.5;
const float ambientOcclusionLevel = 1.0;

// Forme de la carte d'ombre : le centre est grossi, les lointains tassés.
#define SHADOW_BEND 0.86
#define SHADOW_DEPTH 0.5
