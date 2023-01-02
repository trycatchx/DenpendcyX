package plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import plugin.utils.DependenciesHelper

/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/24
 * copyright TCL+
 */
open class AppProjectDependencies(
    var project: Project,
    var android: AppExtension,
    var listener: ((finish: Boolean) -> Unit)? = null) : DependencyResolutionListener {


    var mAllChildProjectDependenciesList = arrayListOf<ChildProjectDependencies>()
    lateinit var mDependenciesHelper: DependenciesHelper
    init {
        project.gradle.addListener(this)
    }

    override fun beforeResolve(p0: ResolvableDependencies) {
        project.gradle.removeListener(this)
        project.rootProject.allprojects.forEach {
            //剔除 app 和 rootProject
            if (it != project.rootProject && it.childProjects.size <= 0) {
                //每一个 project 的依赖，都在 ProjectDependencies 里面解决
                val project = ChildProjectDependencies(it, android)
                mAllChildProjectDependenciesList.add(project)
            }
        }

        mDependenciesHelper = DependenciesHelper(mAllChildProjectDependenciesList)
        listener?.invoke(true)
    }

    override fun afterResolve(p0: ResolvableDependencies) {

    }

}