package io.github.nexfusion.core

/** Extension functions for [kotlin.Result] to simplify common patterns. */
inline fun <T> Result<T>.orElse(other: () -> Result<T>): Result<T> =
    if (isSuccess) this else other()

/** Returns the value if successful, otherwise returns [defaultValue]. */
inline fun <T> Result<T>.getOrDefault(defaultValue: T): T =
    getOrElse { defaultValue }

/** Returns the value if successful, otherwise throws the exception. */
inline fun <T> Result<T>.getOrThrow(): T =
    getOrElse { throw it }

/** Maps the value if successful, otherwise returns [defaultValue]. */
inline fun <T, R> Result<T>.mapOrDefault(transform: (T) -> R, defaultValue: R): R =
    fold({ transform(it) }, { defaultValue })

/** FlatMaps the value if successful, otherwise returns [defaultValue]. */
inline fun <T, R> Result<T>.flatMapOrDefault(transform: (T) -> Result<R>, defaultValue: R): R =
    fold(
        { transform(it).getOrDefault(defaultValue) },
        { defaultValue }
    )

/** Converts to a nullable value, returning null on failure. */
inline fun <T> Result<T>.toNullable(): T? =
    getOrNull()

/** Chains multiple fallbacks: tries this, then each fallback in order until one succeeds. */
inline fun <T> Result<T>.orElse(vararg fallbacks: () -> Result<T>): Result<T> =
    fold(
        { this },
        { e ->
            for (fallback in fallbacks) {
                val result = fallback()
                if (result.isSuccess) return result
            }
            Result.failure(e)
        }
    )