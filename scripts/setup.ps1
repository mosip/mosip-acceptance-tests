$Logfile = "./../debug.log"

$partnerDemoServiceUrl = "https://mosip.s3-us-west-2.amazonaws.com/authentication-partnerdemo-service.jar"
$partnerDemoServiceJarPath = "./../ivv-orchestrator/dependency_jars/authentication-partnerdemo-service.jar"

$registrationServicesUrl = "https://mosip.s3-us-west-2.amazonaws.com/registration-services-0.9.1.jar"
$registrationServicesJarPath = "./../ivv-registration/services_jar/registration-services.jar"

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
    if(-not $env:JAVA_HOME){
        LogWrite "error" "JAVA_HOME not set"
        return
    } else {
        LogWrite "debug" "JAVA_HOME is set"
    }
#    xcopy "./../dependencies" "./../ivv-orchestrator/local" /i /S;
    LogWrite "debug" "Sucessfully copied dependencies...";

#    DownloadFile $partnerDemoServiceUrl $partnerDemoServiceJarPath;
    LogWrite "debug" "Sucessfully downloaded partner-service jar...";

#    DownloadFile $registrationServicesUrl $registrationServicesJarPath;
    LogWrite "debug" "Sucessfully downloaded registration-services jar...";

    mvn clean install -f ./../pom.xml;
    LogWrite "debug" "Building project...";

    timeout /t 10;
} catch {
    LogWrite  "error" $_
}


