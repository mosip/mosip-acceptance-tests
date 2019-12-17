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

function DownloadFile($url, $targetFile) {
    $uri = New-Object "System.Uri" "$url"
    $request = [System.Net.HttpWebRequest]::Create($uri)
    $request.set_Timeout(15000) #15 second timeout
    $response = $request.GetResponse()
    $totalLength = [System.Math]::Floor($response.get_ContentLength()/1024)
    $responseStream = $response.GetResponseStream()
    $targetStream = New-Object -TypeName System.IO.FileStream -ArgumentList $targetFile, Create
    $buffer = new-object byte[] 100KB
    $count = $responseStream.Read($buffer,0,$buffer.length)
    $downloadedBytes = $count

    while ($count -gt 0){
        $targetStream.Write($buffer, 0, $count)
        $count = $responseStream.Read($buffer,0,$buffer.length)
        $downloadedBytes = $downloadedBytes + $count
        Write-Progress -activity "Downloading file '$($url.split('/') | Select -Last 1)'" -status "Downloaded ($([System.Math]::Floor($downloadedBytes/1024))K of $($totalLength)K): " -PercentComplete ((([System.Math]::Floor($downloadedBytes/1024)) / $totalLength)  * 100)
    }

    LogWrite "debug" "Finished downloading file '$($url.split('/') | Select -Last 1)'"
    $targetStream.Flush()
    $targetStream.Close()
    $targetStream.Dispose()
    $responseStream.Dispose()
}

try {
    $rootPath = Resolve-Path (Join-Path (pwd) "./../..")
    LogWrite "debug" "rootpath: $($rootPath)"

    $partnerDemoServiceUrl = "https://mosip.s3-us-west-2.amazonaws.com/0.9.1/authentication-partnerdemo-service.jar"
    $partnerDemoServiceJarPath = "$($rootPath)/ivv-orchestrator/dependency_jars/authentication-partnerdemo-service.jar"

    $registrationServicesUrl = "https://mosip.s3-us-west-2.amazonaws.com/0.9.1/registration-services.jar"
    $registrationServicesJarPath = "$($rootPath)/ivv-registration/services_jar/registration-services.jar"

    $databaseUrl = "https://mosip.s3-us-west-2.amazonaws.com/0.9.1/db.zip"
    $databaseLocalZipPath = "$($rootPath)/ivv-orchestrator/db.zip"
    $databaseLocalFolderPath = "$($rootPath)/ivv-orchestrator"

    if(-not $env:JAVA_HOME){
        LogWrite "error" "JAVA_HOME not set"
        return
    } else {
        LogWrite "debug" "JAVA_HOME is set"
    }

    Copy-Item -Path "$($rootPath)/dependencies" -Destination "$($rootPath)/ivv-orchestrator/local" -Force -Recurse;
    LogWrite "debug" "Sucessfully copied dependencies.";

    Copy-Item -Path "$($rootPath)/ivv-orchestrator/config.properties.example" "$($rootPath)/ivv-orchestrator/config.properties" -Force;
    LogWrite "debug" "Sucessfully copied config.properties.";

    LogWrite "debug" "Downloading partner-service jar to path: $($partnerDemoServiceJarPath)";
    DownloadFile $partnerDemoServiceUrl $partnerDemoServiceJarPath;
    LogWrite "debug" "Sucessfully downloaded partner-service jar.";

    LogWrite "debug" "Downloading registration-services jar to path: $($registrationServicesJarPath)";
    DownloadFile $registrationServicesUrl $registrationServicesJarPath;
    LogWrite "debug" "Sucessfully downloaded registration-services jar.";

    LogWrite "debug" "Downloading derby database to path: $($databaseLocalZipPath)";
    DownloadFile $databaseUrl $databaseLocalZipPath;
    LogWrite "debug" "Sucessfully downloaded derby database.";

    LogWrite "debug" "Unzipping derby database to path: $($databaseLocalFolderPath)";
    Expand-Archive -LiteralPath $databaseLocalZipPath -DestinationPath $databaseLocalFolderPath -Force

    LogWrite "debug" "Removing database zip file.";
    Remove-Item $databaseLocalZipPath

    mvn clean install `-f $rootPath/pom.xml `-Dmaven.test.skip=true;
    LogWrite "debug" "Build successful.";

    LogWrite "debug" "Finished setup. Before executing run script please add the configuration properties in ivv-orchestrator/config.properties. Press enter key to continue...";
    Read-Host -Prompt "Press Enter to exit"
} catch {
    LogWrite  "error" $_
}


