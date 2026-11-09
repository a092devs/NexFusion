package io.github.nexfusion

/**
 * Centralized package names and class names used across patches.
 * Centralizes all hardcoded package/class names for easier maintenance.
 */
object PackageNames {
    // Google / Android framework
    object Google {
        object Gms {
            const val MEASUREMENT = "com.google.android.gms.measurement.AppMeasurement"
            object Analytics {
                const val FIREBASE_ANALYTICS = "com.google.firebase.analytics.FirebaseAnalytics"
                const val GOOGLE_ANALYTICS = "com.google.android.gms.analytics.GoogleAnalytics"
                const val TAG_MANAGER = "com.google.android.gms.tagmanager.TagManager"
                const val CONTAINER_HOLDER = "com.google.android.gms.tagmanager.ContainerHolder"
            }
            object Ads {
                const val GOOGLE_ADS = "https://googleads.g.doubleclick.net"
                const val PUBADS = "https://pubads.g.doubleclick.net"
                const val STATIC_DOUBLECLICK = "https://static.doubleclick.net"
                const val CRASHLYTICS_SETTINGS = "https://firebase-settings.crashlytics.com/spi/v2/platforms/android/gmp/"
                const val APP_MEASUREMENT_A = "https://app-measurement.com/a"
                const val APP_MEASUREMENT_SD = "https://app-measurement.com/s/d"
            }
            object Firebase {
                const val MEASUREMENT = "com.google.android.gms.measurement.AppMeasurement"
                const val FIREBASE_ANALYTICS = "com.google.firebase.analytics.FirebaseAnalytics"
                const val FIREBASE_CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
                const val FIREBASE_PERF = "com.google.firebase.perf.metrics.Trace"
                const val FIREBASE_INSTALLATIONS = "com.google.firebase.installations.FirebaseInstallations"
                const val DYNAMIC_LINKS = "com.google.firebase.dynamiclinks.FirebaseDynamicLinks"
                const val FIREBASE_AUTH = "com.google.firebase.auth.FirebaseAuth"
                const val FIREBASE_DATABASE = "com.google.firebase.database.FirebaseDatabase"
                const val FIREBASE_FIRESTORE = "com.google.firebase.firestore.FirebaseFirestore"
                const val FIREBASE_STORAGE = "com.google.firebase.storage.FirebaseStorage"
                const val FIREBASE_MESSAGING = "com.google.firebase.messaging.FirebaseMessaging"
                const val FIREBASE_REMOTE_CONFIG = "com.google.firebase.remoteconfig.FirebaseRemoteConfig"
                const val FIREBASE_INSTALLATIONS = "com.google.firebase.installations.FirebaseInstallations"
                const val FIREBASE_ML = "com.google.firebase.ml.FirebaseML"
                const val FIREBASE_FUNCTIONS = "com.google.firebase.functions.FirebaseFunctions"
                const val FIREBASE_PERF = "com.google.firebase.perf.FirebasePerformance"
                const val CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
                const val FIREBASE_ML = "com.google.firebase.ml.FirebaseML"
            }
            object Ads {
                const val GOOGLE_ADS = "https://googleads.g.doubleclick.net"
                const val PUBADS = "https://pubads.g.doubleclick.net"
                const val STATIC_DOUBLECLICK = "https://static.doubleclick.net"
                const val CRASHLYTICS_SETTINGS = "https://firebase-settings.crashlytics.com/spi/v2/platforms/android/gmp/"
                const val APP_MEASUREMENT_A = "https://app-measurement.com/a"
                const val APP_MEASUREMENT_SD = "https://app-measurement.com/s/d"
            }
            object Gms {
                const val MEASUREMENT = "com.google.android.gms.measurement.AppMeasurement"
                const val ADS = "com.google.android.gms.ads"
                const val AUTH = "com.google.android.gms.auth"
                const val DRIVE = "com.google.android.gms.drive"
                const val FIT = "com.google.android.gms.fitness"
                const val GAMES = "com.google.android.gms.games"
                const val LOCATION = "com.google.android.gms.location"
                const val MAPS = "com.google.android.gms.maps"
                const val NEARBY = "com.google.android.gms.nearby"
                const val PLACES = "com.google.android.gms.places"
                const val PLUS = "com.google.android.gms.plus"
                const val WALLET = "com.google.android.gms.wallet"
                const val WEARABLE = "com.google.android.gms.wearable"
            }
        }
        object Ads {
            const val ADMOB = "com.google.android.gms.ads"
            const val ADS = "com.google.android.gms.ads"
        }
        object PlayServices {
            const val BASE = "com.google.android.gms"
            const val AUTH = "com.google.android.gms.auth"
            const val DRIVE = "com.google.android.gms.drive"
            const val FIT = "com.google.android.gms.fitness"
            const val GAMES = "com.google.android.gms.games"
            const val LOCATION = "com.google.android.gms.location"
            const val MAPS = "com.google.android.gms.maps"
            const val NEARBY = "com.google.android.gms.nearby"
            const val PLACES = "com.google.android.gms.places"
            const val PLUS = "com.google.android.gms.plus"
            const val WALLET = "com.google.android.gms.wallet"
            const val WEARABLE = "com.google.android.gms.wearable"
        }
    }
    object Firebase {
        object Analytics {
            const val FIREBASE_ANALYTICS = "com.google.firebase.analytics.FirebaseAnalytics"
        }
        object Crashlytics {
            const val CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
        }
        object Perf {
            const val TRACE = "com.google.firebase.perf.metrics.Trace"
            const val FIREBASE_PERF = "com.google.firebase.perf.FirebasePerformance"
            const val METRIC = "com.google.firebase.perf.metrics.Metric"
        }
        object Analytics {
            const val FIREBASE_ANALYTICS = "com.google.firebase.analytics.FirebaseAnalytics"
        }
        object Crashlytics {
            const val CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
        }
        object Perf {
            const val TRACE = "com.google.firebase.perf.metrics.Trace"
            const val FIREBASE_PERF = "com.google.firebase.perf.FirebasePerformance"
        }
        object Auth {
            const val FIREBASE_AUTH = "com.google.firebase.auth.FirebaseAuth"
        }
        object Database {
            const val FIREBASE_DATABASE = "com.google.firebase.database.FirebaseDatabase"
        }
        object Firestore {
            const val FIREBASE_FIRESTORE = "com.google.firebase.firestore.FirebaseFirestore"
        }
        object Storage {
            const val FIREBASE_STORAGE = "com.google.firebase.storage.FirebaseStorage"
        }
        object Messaging {
            const val FIREBASE_MESSAGING = "com.google.firebase.messaging.FirebaseMessaging"
        }
        object RemoteConfig {
            const val FIREBASE_REMOTE_CONFIG = "com.google.firebase.remoteconfig.FirebaseRemoteConfig"
        }
        object Installations {
            const val FIREBASE_INSTALLATIONS = "com.google.firebase.installations.FirebaseInstallations"
        }
        object Ml {
            const val FIREBASE_ML = "com.google.firebase.ml.FirebaseML"
        }
        object Functions {
            const val FIREBASE_FUNCTIONS = "com.google.firebase.functions.FirebaseFunctions"
        }
        object Perf {
            const val FIREBASE_PERF = "com.google.firebase.perf.FirebasePerformance"
        }
        object Crashlytics {
            const val CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
        }
        object Ml {
            const val FIREBASE_ML = "com.google.firebase.ml.FirebaseML"
        }
        object Functions {
            const val FIREBASE_FUNCTIONS = "com.google.firebase.functions.FirebaseFunctions"
        }
        object Perf {
            const val FIREBASE_PERF = "com.google.firebase.perf.FirebasePerformance"
        }
        object Crashlytics {
            const val CRASHLYTICS = "com.google.firebase.crashlytics.FirebaseCrashlytics"
        }
        object Ads {
            const val ADMOB = "com.google.android.gms.ads"
            const val ADS = "com.google.android.gms.ads"
        }
        object PlayServices {
            const val BASE = "com.google.android.gms"
            const val AUTH = "com.google.android.gms.auth"
            const val DRIVE = "com.google.android.gms.drive"
            const val FIT = "com.google.android.gms.fitness"
            const val GAMES = "com.google.android.gms.games"
            const val LOCATION = "com.google.android.gms.location"
            const val MAPS = "com.google.android.gms.maps"
            const val NEARBY = "com.google.android.gms.nearby"
            const val PLACES = "com.google.android.gms.places"
            const val PLUS = "com.google.android.gms.plus"
            const val WALLET = "com.google.android.gms.wallet"
            const val WEARABLE = "com.google.android.gms.wearable"
        }
    }
    object Ads {
        const val ADMOB = "com.google.android.gms.ads"
        const val ADS = "com.google.android.gms.ads"
    }
    object PlayServices {
        const val BASE = "com.google.android.gms"
        const val AUTH = "com.google.android.gms.auth"
        const val DRIVE = "com.google.android.gms.drive"
        const val FIT = "com.google.android.gms.fitness"
        const val GAMES = "com.google.android.gms.games"
        const val LOCATION = "com.google.android.gms.location"
        const val MAPS = "com.google.android.gms.maps"
        const val NEARBY = "com.google.android.gms.nearby"
        const val PLACES = "com.google.android.gms.places"
        const val PLUS = "com.google.android.gms.plus"
        const val WALLET = "com.google.android.gms.wallet"
        const val WEARABLE = "com.google.android.gms.wearable"
    }
}
object Braze {
    const val SDK_ENDPOINT = "https://sdk.iad-01.braze.com/api/v3/"
    const val SONDHEIM = "https://sondheim.braze.com/api/v3/"
}
object Sentry {
    const val DSN = "https://o438986.ingest.sentry.io/api/5405103/store/?sentry_key=524610463d1141a58e7f57053dc7ef87"
    const val ENDPOINT = "https://o438986.ingest.sentry.io/api/5405103/store/"
}
object GoogleAds {
    const val GOOGLE_ADS = "https://googleads.g.doubleclick.net"
    const val PUBADS = "https://pubads.g.doubleclick.net"
    const val STATIC_DOUBLECLICK = "https://static.doubleclick.net"
    const val CRASHLYTICS_SETTINGS = "https://firebase-settings.crashlytics.com/spi/v2/platforms/android/gmp/"
    const val APP_MEASUREMENT_A = "https://app-measurement.com/a"
    const val APP_MEASUREMENT_SD = "https://app-measurement.com/s/d"
}
object AppsFlyer {
    const val API_V5 = "https://api2.appsflyer.com/api/v5/"
}
object Adjust {
    const val ENDPOINT = "https://app.adjust.com/"
}
object Sentry {
    const val DSN = "https://o438986.ingest.sentry.io/api/5405103/store/?sentry_key=524610463d1141a58e7f57053dc7ef87"
}
object AppsFlyer {
    const val API_V5 = "https://api2.appsflyer.com/api/v5/"
}
object Adjust {
    const val ENDPOINT = "https://app.adjust.com/"
}
object Sentry {
    const val DSN = "https://o438986.ingest.sentry.io/api/5405103/store/?sentry_key=524610463d1141a58e7f57053dc7ef87"
}
object AppsFlyer {
    const val API_V5 = "https://api2.appsflyer.com/api/v5/"
}
object Adjust {
    const val ENDPOINT = "https://app.adjust.com/"
}
object Plaid {
    const val ANALYTICS = "https://analytics.plaid.com/sentry/api/"
}
object ScribeUp {
    const val URL = "https://api.scribeup.io"
}