param(
  [int]$UserId = 1
)

$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$avatarDir = Join-Path $root "frontend\public\avatars\seed-contacts"
New-Item -ItemType Directory -Force -Path $avatarDir | Out-Null

$people = @(
  @{ Name="Alex Chen"; Company="Starline Tech"; Position="Product Manager"; Address="100 Century Ave, Shanghai"; Birthday="1992-03-14"; GroupId=5; Favorite=1 },
  @{ Name="Bella Lin"; Company="Cedar Studio"; Position="Visual Designer"; Address="88 Wensan Rd, Hangzhou"; Birthday="1995-07-22"; GroupId=7; Favorite=0 },
  @{ Name="Caleb Zhou"; Company="Blueprint Advisory"; Position="Client Consultant"; Address="66 Jianguo Rd, Beijing"; Birthday="1989-11-05"; GroupId=8; Favorite=1 },
  @{ Name="Diana Zhao"; Company="Morning Education"; Position="Course Lead"; Address="21 Zhongshan Rd, Nanjing"; Birthday="1993-01-30"; GroupId=7; Favorite=0 },
  @{ Name="Ethan Sun"; Company="Sea Whale Logistics"; Position="Operations Specialist"; Address="9 Tech Park, Shenzhen"; Birthday="1991-05-18"; GroupId=8; Favorite=0 },
  @{ Name="Fiona Li"; Company="Northstar Capital"; Position="Investment Manager"; Address="12 Zhujiang New Town, Guangzhou"; Birthday="1988-09-09"; GroupId=8; Favorite=1 },
  @{ Name="Grace Wu"; Company="Orange Media"; Position="Content Planner"; Address="28 Chunxi Rd, Chengdu"; Birthday="1996-12-03"; GroupId=7; Favorite=0 },
  @{ Name="Henry Zheng"; Company="Pine Software"; Position="Backend Engineer"; Address="328 Xinghu St, Suzhou"; Birthday="1990-04-25"; GroupId=8; Favorite=1 },
  @{ Name="Ivy Wang"; Company="Shanhai Health"; Position="Marketing Manager"; Address="44 Zhongnan Rd, Wuhan"; Birthday="1994-08-16"; GroupId=7; Favorite=0 },
  @{ Name="Jack Xu"; Company="Ginkgo Bookstore"; Position="Store Manager"; Address="6 Jiefangbei, Chongqing"; Birthday="1997-06-01"; GroupId=6; Favorite=0 },
  @{ Name="Kevin He"; Company="Flying Deer Auto"; Position="Sales Lead"; Address="118 Nanjing Rd, Tianjin"; Birthday="1987-02-11"; GroupId=8; Favorite=1 },
  @{ Name="Luna Guo"; Company="Lime Dining"; Position="Brand Manager"; Address="19 Hubin S Rd, Xiamen"; Birthday="1992-10-27"; GroupId=7; Favorite=0 },
  @{ Name="Mason Ma"; Company="Prism Data"; Position="Data Analyst"; Address="50 Gaoxin Rd, Xian"; Birthday="1995-03-08"; GroupId=8; Favorite=0 },
  @{ Name="Nora Hu"; Company="White Tower Build"; Position="Project Manager"; Address="2 Lushan S Rd, Changsha"; Birthday="1986-07-19"; GroupId=8; Favorite=1 },
  @{ Name="Owen Gao"; Company="Glimmer Charity"; Position="Volunteer Coordinator"; Address="31 Hong Kong Middle Rd, Qingdao"; Birthday="1998-11-13"; GroupId=7; Favorite=0 },
  @{ Name="Penny Luo"; Company="Frontier Games"; Position="Interaction Designer"; Address="27 Zhongguancun Ave, Beijing"; Birthday="1993-09-24"; GroupId=8; Favorite=0 },
  @{ Name="Quinn Liang"; Company="Forest Travel"; Position="Travel Consultant"; Address="5 Zhongshan S Rd, Guilin"; Birthday="1991-01-06"; GroupId=7; Favorite=1 },
  @{ Name="Ryan Song"; Company="Long Bridge Trade"; Position="Procurement Manager"; Address="99 Fuming Rd, Ningbo"; Birthday="1989-05-29"; GroupId=8; Favorite=0 },
  @{ Name="Sophie Tang"; Company="Bamboo Photo"; Position="Photographer"; Address="17 Qingnian Rd, Kunming"; Birthday="1996-04-12"; GroupId=7; Favorite=0 },
  @{ Name="Theo Xie"; Company="Particle Lab"; Position="Frontend Engineer"; Address="10 Science Ave, Hefei"; Birthday="1994-12-20"; GroupId=8; Favorite=1 },
  @{ Name="Uma Cao"; Company="Qingchuan Law"; Position="Legal Assistant"; Address="77 Quancheng Rd, Jinan"; Birthday="1992-02-26"; GroupId=7; Favorite=0 },
  @{ Name="Victor Ding"; Company="Hetian Agriculture"; Position="Regional Manager"; Address="60 Nongye Rd, Zhengzhou"; Birthday="1988-08-04"; GroupId=8; Favorite=0 },
  @{ Name="Wendy Ye"; Company="Ink Cloud Network"; Position="QA Engineer"; Address="158 Wusi Rd, Fuzhou"; Birthday="1997-10-10"; GroupId=7; Favorite=0 },
  @{ Name="Xavier Jiang"; Company="Luming Music"; Position="Music Teacher"; Address="1 Xuefu Rd, Harbin"; Birthday="1990-06-17"; GroupId=6; Favorite=1 },
  @{ Name="Yara Fan"; Company="Azalea Hotel"; Position="Room Manager"; Address="23 Yangming Rd, Nanchang"; Birthday="1987-12-31"; GroupId=8; Favorite=0 },
  @{ Name="Zane Yao"; Company="Blue Whale Insurance"; Position="Claims Specialist"; Address="45 Renmin Rd, Dalian"; Birthday="1995-01-15"; GroupId=7; Favorite=0 },
  @{ Name="Ariel Pan"; Company="Qiming Manufacturing"; Position="Quality Engineer"; Address="188 Taihu Ave, Wuxi"; Birthday="1991-09-02"; GroupId=8; Favorite=1 },
  @{ Name="Blake Zou"; Company="Oasis Energy"; Position="Business Development"; Address="99 Changfeng St, Taiyuan"; Birthday="1993-05-07"; GroupId=8; Favorite=0 },
  @{ Name="Clara Qin"; Company="Knownew Press"; Position="Editor"; Address="25 Nanjing N St, Shenyang"; Birthday="1998-03-23"; GroupId=7; Favorite=0 },
  @{ Name="Derek Du"; Company="Redwood Medical"; Position="Medical Assistant"; Address="68 Beijing Rd, Guiyang"; Birthday="1990-11-28"; GroupId=6; Favorite=1 }
)

$remarks = @(
  "Key account, monthly follow-up",
  "Design partner contact",
  "Consulting project contact",
  "Course communication contact",
  "Logistics coordination contact",
  "Investment business contact",
  "Content partnership contact",
  "Technical integration contact",
  "Marketing campaign contact",
  "Store operations contact",
  "Sales priority contact",
  "Brand partnership contact",
  "Data analysis contact",
  "Project delivery contact",
  "Charity event contact",
  "Product experience contact",
  "Travel business contact",
  "Procurement coordination contact",
  "Photography partner contact",
  "Frontend technical contact",
  "Legal support contact",
  "Regional business contact",
  "QA coordination contact",
  "Music course contact",
  "Hotel business contact",
  "Insurance claim contact",
  "Quality management contact",
  "Energy business contact",
  "Publishing editor contact",
  "Medical coordination contact"
)

$values = New-Object System.Collections.Generic.List[string]
for ($i = 0; $i -lt $people.Count; $i++) {
  $number = $i + 1
  $avatarPath = Join-Path $avatarDir ("avatar-{0:D2}.jpg" -f $number)
  if (-not (Test-Path $avatarPath)) {
    throw "Missing seed avatar: $avatarPath"
  }

  $bytes = [System.IO.File]::ReadAllBytes($avatarPath)
  if ($bytes.Length -lt 3 -or $bytes[0] -ne 0xFF -or $bytes[1] -ne 0xD8) {
    throw "Seed avatar is not a JPG: $avatarPath"
  }
  $avatarData = "/avatars/seed-contacts/avatar-{0:D2}.jpg" -f $number
  $person = $people[$i]
  $phone = "1392606{0:D4}" -f $number
  $email = "seed{0:D2}@contact-master.local" -f $number
  $remark = $remarks[$i]
  $groupName = switch ($person.GroupId) {
    6 { "家人" }
    7 { "朋友" }
    8 { "同事" }
    default { "默认分组" }
  }
  $groupExpression = "(SELECT id FROM contact_groups WHERE user_id = $UserId AND name = '$groupName' LIMIT 1)"

  foreach ($key in @("Name","Company","Position","Address","Birthday")) {
    $person[$key] = $person[$key].Replace("'", "''")
  }
  $avatarData = $avatarData.Replace("'", "''")
  $remark = $remark.Replace("'", "''")

  $values.Add(("({0},{1},'{2}','{3}','{4}','{5}','{6}','{7}','{8}','{9}','{10}',{11},0,NOW(6),NOW(6))" -f
    $UserId,
    $groupExpression,
    $person.Name,
    $phone,
    $email,
    $person.Company,
    $person.Position,
    $person.Address,
    $person.Birthday,
    $remark,
    $avatarData,
    $person.Favorite
  ))
}

$deletePhones = (1..30 | ForEach-Object { "'1392606{0:D4}'" -f $_ }) -join ","
$sql = @"
DELETE FROM contacts
WHERE user_id = $UserId
  AND phone IN ($deletePhones);

INSERT INTO contacts
  (user_id, group_id, name, phone, email, company, position, address, birthday, remark, avatar_data, favorite, deleted, created_at, updated_at)
VALUES
$($values -join ",`n");

SELECT COUNT(*) AS seeded_contacts
FROM contacts
WHERE user_id = $UserId
  AND phone IN ($deletePhones);
"@

$sqlPath = Join-Path ([System.IO.Path]::GetTempPath()) ("seed-contacts-{0}.sql" -f [System.Guid]::NewGuid())
try {
  [System.IO.File]::WriteAllText($sqlPath, $sql, [System.Text.Encoding]::UTF8)
  mysql -uroot -proot contact_master --default-character-set=utf8mb4 -e "source $($sqlPath.Replace('\','/'))"
} finally {
  if (Test-Path $sqlPath) {
    Remove-Item -LiteralPath $sqlPath -Force
  }
}
