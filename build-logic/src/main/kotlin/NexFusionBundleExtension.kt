import org.gradle.api.provider.Property

interface NexFusionBundleExtension {
    /** Fully qualified name of the class implementing `io.github.nexfusion.PatchBundle`. */
    val entry: Property<String>

    /** Shown in the NexFusion manager. */
    val author: Property<String>

    /** Resource name of a string describing the bundle, shown in the NexFusion manager. */
    val descriptionResource: Property<String>

    /**
     * Package id of the bundle's resources, 0x02 to 0x7e. Bundles loaded into the same app need
     * distinct ids, and must not use the app's 0x7f. NexFusion refuses to install a bundle whose id
     * collides with an installed bundle.
     */
    val resourcePackageId: Property<Int>

    /**
     * Compatibility series (e.g. "3.2"). The manager checks this against its own major.minor series.
     */
    val compatibilitySeries: Property<String>

    /** Minimum compatible manager app version (e.g. "3.2.0"). */
    val minManagerVersion: Property<String>

    /** Maximum compatible manager app version, or empty if unbounded. */
    val maxManagerVersion: Property<String>
}
