param(
  [string]$BaseUrl = "http://localhost:8080",
  [string]$User1 = "e2e_ps_1",
  [string]$User2 = "e2e_ps_2",
  [string]$Pass1 = "pass1",
  [string]$Pass2 = "pass2",
  [string]$Email1 = "e2e_ps_1@example.com",
  [string]$Email2 = "e2e_ps_2@example.com"
)

function Invoke-JsonPost($Url, $Body) {
  Invoke-RestMethod -Uri $Url -Method Post -ContentType "application/json" -Body ($Body | ConvertTo-Json)
}

Invoke-JsonPost "$BaseUrl/api/v1/users" @{ username = $User1; email = $Email1; password = $Pass1 } | Out-Null
Invoke-JsonPost "$BaseUrl/api/v1/users" @{ username = $User2; email = $Email2; password = $Pass2 } | Out-Null

$token1 = (Invoke-JsonPost "$BaseUrl/api/v1/tokens" @{ username = $User1; password = $Pass1 }).token
$token2 = (Invoke-JsonPost "$BaseUrl/api/v1/tokens" @{ username = $User2; password = $Pass2 }).token

$headers1 = @{ Authorization = "Bearer $token1" }
$headers2 = @{ Authorization = "Bearer $token2" }

Invoke-RestMethod -Uri "$BaseUrl/api/v1/profiles/me" -Method Patch -Headers $headers1 -ContentType "application/json" -Body (@{ score = 100 } | ConvertTo-Json) | Out-Null
Invoke-RestMethod -Uri "$BaseUrl/api/v1/profiles/me" -Method Patch -Headers $headers2 -ContentType "application/json" -Body (@{ score = 50 } | ConvertTo-Json) | Out-Null

Invoke-RestMethod -Uri "$BaseUrl/api/v1/profiles/me" -Method Get -Headers $headers1 | Out-Null
Invoke-RestMethod -Uri "$BaseUrl/api/v1/ratings?sortBy=points&limit=10" -Method Get -Headers $headers1 | Out-Null

Write-Host "E2E requests completed"
