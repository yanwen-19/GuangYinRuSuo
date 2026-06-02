@rem Gradle wrapper bootstrap for Windows
@echo off
set DIRNAME=%~dp0
if "%JAVA_HOME%" == "" (
  echo ERROR: JAVA_HOME is not set.
  exit /b 1
)
"%JAVA_HOME%/bin/java" -classpath "%DIRNAME%/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
