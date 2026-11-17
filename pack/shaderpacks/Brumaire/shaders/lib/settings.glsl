// Brumaire : réglages. Les valeurs entre crochets sont celles que propose le menu d'Iris.

// Brume
#define MIST_DENSITY 1.0 // [0.5 0.75 1.0 1.25 1.5 2.0]
#define MIST_HEIGHT 16.0 // [12.0 16.0 24.0 32.0 48.0]
#define MIST_LIFT 0.7 // [0.0 0.3 0.5 0.7 0.85 1.0]
#define MIST_RING 1.0 // [0.0 0.5 1.0 1.5 2.0]
#define MIST_RING_MAX 512.0 // [256.0 384.0 512.0 768.0 1024.0]
#define AIR_DENSITY 1.0 // [0.0 0.5 0.75 1.0 1.25 1.5 2.0]

// Nuages
#define CLOUD_STEPS 16 // [0 10 16 24 32]
#define CLOUD_HEIGHT 220.0 // [180.0 200.0 220.0 250.0 280.0]
#define CLOUD_COVERAGE 0.45 // [0.2 0.3 0.45 0.6 0.75]
#define CLOUD_SPEED 1.0 // [0.0 0.5 1.0 2.0 3.0]
#define CLOUD_LIGHT 1.0 // [0.6 0.8 1.0 1.2 1.5]
#define CLOUD_RANGE 3000.0 // [1500.0 2000.0 3000.0 4000.0]
#define CLOUD_SHADOWS 0.8 // [0.0 0.4 0.6 0.8 1.0]

// Lumière
#define SHADOW_FILTER 8 // [1 4 6 8 12 16]
#define SHADOW_SOFTNESS 1.0 // [0.0 0.5 0.75 1.0 1.5 2.0]
#define LIGHT_SHAFTS 10 // [0 6 10 16 24]
#define SHAFT_STRENGTH 1.0 // [0.0 0.5 0.75 1.0 1.25 1.5]
#define BLOOM
#define AO_SAMPLES 10 // [0 6 10 16]
#define AO_RADIUS 1.2 // [0.6 0.9 1.2 1.6 2.0]
#define AO_STRENGTH 1.6 // [0.8 1.2 1.6 2.0 2.5]
#define BLOOM_STRENGTH 1.0 // [0.5 0.75 1.0 1.25 1.5]
#define COLORED_LIGHT 1 // [0 1]

// Vent et eau
#define WIND_STRENGTH 1.0 // [0.0 0.5 0.75 1.0 1.25 1.5]
#define WATER_WAVES 1.0 // [0.0 0.5 1.0 1.5]
#define WATER_MURK 1.0 // [0.5 0.75 1.0 1.5 2.0]
#define WATER_PIXELS 1 // [0 1]
#define WATER_CAUSTICS 2 // [0 1 2]
#define WATER_REFLECTIONS 20 // [0 12 20 32]
#define VOXEL_REFLECTIONS 64 // [0 32 64 96]
#define WATER_MOTES 1 // [0 1]
#define WATER_WAKES 1 // [0 1]
#define WATER_FOAM 1 // [0 1]
#define AIR_MOTES 1 // [0 1]

// Pluie
#define SHADER_RAIN 1 // [0 1]
#define WETNESS 1.0 // [0.0 0.5 0.75 1.0 1.25]
#define PUDDLES 1 // [0 1]
#define PUDDLE_AMOUNT 1.0 // [0.5 0.75 1.0 1.25 1.5]
#define RAIN_CURTAINS 1.0 // [0.0 0.5 1.0 1.5 2.0]
#define WET_REFLECTIONS 12 // [0 8 12 20]

// Neige et chaleur
#define SNOW_COVER 1 // [0 1]
#define FROST 1 // [0 1]
#define HEAT_SHIMMER 1 // [0 1]
#define TRACKS 1 // [0 1]
#define TRACK_MINUTES 4.0 // [1.0 2.0 4.0 8.0 15.0]

// Couleurs
#define OLIVE 0.8 // [0.0 0.2 0.4 0.6 0.8 1.0]
#define SATURATION 1.0 // [0.7 0.8 0.9 1.0 1.1 1.2]
#define CONTRAST 1.0 // [0.9 0.95 1.0 1.05 1.1 1.15]
#define EXPOSURE 1.0 // [0.7 0.85 1.0 1.15 1.3]

// Image
#define SMAA 2 // [0 1 2 3]

const int shadowMapResolution = 2048; // [1024 1536 2048 3072 4096]
const float shadowDistance = 112.0; // [64.0 80.0 96.0 112.0 128.0 160.0 192.0]
const float sunPathRotation = -28.0; // [-40.0 -28.0 -15.0 0.0 15.0 28.0 40.0]
const bool shadowHardwareFiltering0 = false;
const bool shadowHardwareFiltering1 = true;
const float shadowDistanceRenderMul = 1.0;
const float shadowIntervalSize = 2.0;
const float entityShadowDistanceMul = 0.5;
const float ambientOcclusionLevel = 1.0;
// Iris lisse la pluie en « wetness » : le sol se mouille en une minute environ et sèche en trois.
const float wetnessHalflife = 300.0;
const float drynessHalflife = 900.0;

// Forme de la carte d'ombre : le centre est grossi, les lointains tassés.
#define SHADOW_BEND 0.86
#define SHADOW_DEPTH 0.5
