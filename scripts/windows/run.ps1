$Logfile = "./debug.log"

Function LogWrite {
    Param ([string]$type, [string]$msg)
    $date = Get-Date -Format "yyyy-MM-dd:HH:mm:ss"

    switch($type){
        "error" {
            Write-Error $msg
            Add-content $Logfile -value $date" - ERROR - "$msg
        }
        default {
            Write-Output $msg
            Add-content $Logfile -value $date" - DEBUG - "$msg
        }
    }
}

try {
    $rootPath = Resolve-Path (Join-Path (pwd) "./../..")
    LogWrite "debug" "rootpath: $($rootPath)"
    Read-Host -Prompt "`nUser info:`nStart authentication partner demo service if your test includes id-auth DSL: Run this command java -jar ivv-orchestrator/dependency_jars/authentication-partnerdemo-service.jar command in root directory of project, then press enter to continue"
    mvn clean test `-f $rootPath\pom.xml
    Read-Host -Prompt "Press Enter to exit"
} catch {
    LogWrite  "error" $_
}
