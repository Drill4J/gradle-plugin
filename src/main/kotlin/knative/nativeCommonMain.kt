import com.epam.drill.knative.isDevMode
import com.epam.drill.knative.presetName
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Transformer
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

val NamedDomainObjectContainer<KotlinSourceSet>.nativeCommonMain: NamedDomainObjectProvider<KotlinSourceSet>
    get() {
        return if (!isDevMode) named("nativeCommonMain")
        else named("${presetName}Main")
    }

val NamedDomainObjectContainer<KotlinSourceSet>.`nativePosixCommonMain`: NamedDomainObjectProvider<KotlinSourceSet>
    get() {
        if (Os.isFamily(Os.FAMILY_UNIX))
            return if (!isDevMode) named("nativePosixCommonMain")
            else named("${presetName}Main")
        else return object : NamedDomainObjectProvider<KotlinSourceSet> {
            override fun isPresent(): Boolean {
                return false
            }

            override fun configure(action: Action<in KotlinSourceSet>) {

            }

            override fun getOrElse(defaultValue: KotlinSourceSet): KotlinSourceSet {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getName(): String {
                return "stubsourceset"
            }

            override fun <S : Any?> map(transformer: Transformer<out S, in KotlinSourceSet>): Provider<S> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun get(): KotlinSourceSet {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getOrNull(): KotlinSourceSet? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun <S : Any?> flatMap(transformer: Transformer<out Provider<out S>, in KotlinSourceSet>): Provider<S> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun orElse(value: KotlinSourceSet): Provider<KotlinSourceSet> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun orElse(provider: Provider<out KotlinSourceSet>): Provider<KotlinSourceSet> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        }
    }

fun KotlinMultiplatformExtension.configureNative(block: KotlinNativeTarget.() -> Unit) {
    targets.filterIsInstance<KotlinNativeTarget>().forEach {
        block(it)
    }
}