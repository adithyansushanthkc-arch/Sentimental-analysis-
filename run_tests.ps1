$clientSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginClient = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{username="client@example.com"; password="client123"} -WebSession $clientSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "1. Faculty Login Status: $($loginClient.StatusCode), Redirect Location: $($loginClient.Headers['Location'])"

$clientDash = Invoke-WebRequest -Uri "http://localhost:8080/client/dashboard" -WebSession $clientSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "2. Faculty Dashboard Status: $($clientDash.StatusCode)"

$logoutClient = Invoke-WebRequest -Uri "http://localhost:8080/logout" -Method Post -WebSession $clientSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "3. Faculty Logout Status: $($logoutClient.StatusCode), Redirect Location: $($logoutClient.Headers['Location'])"

$afterLogoutDash = Invoke-WebRequest -Uri "http://localhost:8080/client/dashboard" -WebSession $clientSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "4. Post-Logout Client Dashboard Status: $($afterLogoutDash.StatusCode), Redirect Location: $($afterLogoutDash.Headers['Location'])"

$adminSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginAdmin = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{username="admin@example.com"; password="admin123"} -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "5. Admin Login Status: $($loginAdmin.StatusCode), Redirect Location: $($loginAdmin.Headers['Location'])"

$adminDash = Invoke-WebRequest -Uri "http://localhost:8080/admin/dashboard" -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "6. Admin Dashboard Status: $($adminDash.StatusCode)"

$adminDict = Invoke-WebRequest -Uri "http://localhost:8080/admin/dictionary" -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "7. Admin Dictionary Status: $($adminDict.StatusCode)"

$adminDictPos = Invoke-WebRequest -Uri "http://localhost:8080/admin/dictionary?filter=POSITIVE" -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "8. Admin Positive Words Filter Status: $($adminDictPos.StatusCode)"

$adminDictNeg = Invoke-WebRequest -Uri "http://localhost:8080/admin/dictionary?filter=NEGATIVE" -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "9. Admin Negative Words Filter Status: $($adminDictNeg.StatusCode)"

$addWord = Invoke-WebRequest -Uri "http://localhost:8080/admin/dictionary/add" -Method Post -Body @{word="superb"; sentimentType="POSITIVE"; weight=2} -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "10. Admin Add Word Status: $($addWord.StatusCode), Redirect Location: $($addWord.Headers['Location'])"

$logoutAdmin = Invoke-WebRequest -Uri "http://localhost:8080/logout" -Method Post -WebSession $adminSession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "11. Admin Logout Status: $($logoutAdmin.StatusCode), Redirect Location: $($logoutAdmin.Headers['Location'])"

$facultySession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$loginFaculty = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method Post -Body @{username="client@example.com"; password="client123"} -WebSession $facultySession -MaximumRedirection 0 -ErrorAction SilentlyContinue
$facultyAccessAdmin = Invoke-WebRequest -Uri "http://localhost:8080/admin/dashboard" -WebSession $facultySession -MaximumRedirection 0 -ErrorAction SilentlyContinue
Write-Output "12. Faculty Access Admin Dashboard Status: $($facultyAccessAdmin.StatusCode)"
