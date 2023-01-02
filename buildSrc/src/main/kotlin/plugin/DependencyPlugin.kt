package plugin

import com.android.build.gradle.AppExtension
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.gradle.api.*
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency
import plugin.utils.*
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream



/**
 * description:
 * author chaojiong.zhang
 * data: 2021/10/20
 * copyright TCL+
 *
 *
 * mac debug 插件命令 ：export GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 * window debug 插件命令 ：set GRADLE_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
 */
open class DependencyPlugin : Plugin<Project> {

    companion object {
        const val TAG = "RocketXPlugin:"
        const val ASSEMBLE = "assemble"
    }

    lateinit var appProject: Project
    lateinit var android: AppExtension
    lateinit var mAppProjectDependencies: AppProjectDependencies

    override fun apply(project: Project) {
        //应用在 主 project 上，也就是 app module
        if (hasAndroidPlugin(project)) return
        this.appProject = project
        android = project.extensions.getByType(AppExtension::class.java)
        mAppProjectDependencies = AppProjectDependencies(project, android) {
            pritlnDependencyGraph()
        }
    }


    //打印处理完的整个依赖图
    fun pritlnDependencyGraph() {

        // 指定创建的excel文件名称
        // 指定创建的excel文件名称


        val file = File(appProject.projectDir.absolutePath + File.separator + "Dependency.xlsx")
        if (file.exists()) {
            file.delete()
        }
        val outputStream = BufferedOutputStream(FileOutputStream(file))


        // 定义一个工作薄（所有要写入excel的数据，都将保存在workbook中）
        val workbook = XSSFWorkbook()

        createSheet1(workbook)
        createSheet2(workbook)


        // 执行写入操作
        workbook.write(outputStream)
        workbook.close()
        outputStream.flush()
        outputStream.close()

    }


    fun createSheet1(workbook: XSSFWorkbook) {
        // 创建一个sheet
        val sheet = workbook.createSheet("sheet-子依赖")
        //生成标题
        val row = sheet.createRow(0)
        row.createCell(0).apply {
            "Module".let {
                setCellValue(it)
                sheet.setColumnWidth(0, it.length * 256 * 4)
            }
        }
        row.createCell(1).apply {
            "ChildModule".let {
                setCellValue(it)
                sheet.setColumnWidth(1, it.length * 256 * 2)
            }
        }

        var beginRow = 1
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach {
            val curRow = sheet.createRow(beginRow)
            curRow.createCell(0).setCellValue(it.project.path)
            beginRow++
            var beginColumn = 1
            it.allConfigList.forEach {
                if (it.dependencies.size > 0) {
                    it.dependencies.forEach {
                        if (it is DefaultProjectDependency) {
                            curRow.createCell(beginColumn).setCellValue(it.dependencyProject.path)
                            beginColumn++
                            println(TAG + "dependency:" + it.dependencyProject.path)
                        }
                    }
                }
            }
        }

    }


    fun createSheet2(workbook: XSSFWorkbook) {
        // 创建一个sheet
        val sheet = workbook.createSheet("sheet-父依赖")
        //生成标题
        val row = sheet.createRow(0)
        row.createCell(0).apply {
            "Module".let {
                setCellValue(it)
                sheet.setColumnWidth(0, it.length * 256 * 4)
            }
        }

        row.createCell(1).apply {
            "ParentModule".let {
                setCellValue(it)
                sheet.setColumnWidth(1, it.length * 256 * 2)
            }
        }

        var beginRow = 1
        mAppProjectDependencies.mAllChildProjectDependenciesList.forEach {
            val curRow = sheet.createRow(beginRow)
            curRow.createCell(0).setCellValue(it.project.path)
            beginRow++
            var beginColumn = 1

            mAppProjectDependencies.mDependenciesHelper.getFirstLevelParentDependencies(it.project)
                .forEach {
                    curRow.createCell(beginColumn).setCellValue(it.key.path)
                    beginColumn++
                    println(TAG + "parent dependency:" + it.key.path)
                }
        }
    }


}


