# TourAPI 연동 문서

## Festival (festivals 테이블)

※ 축제 목록에서 contentId를 받은 뒤, 해당 ID로 축제 공통·축제소개를 조회합니다.

```
├── contentId (축제 목록 contentid: "1340294")
├── title (축제 공통 title: "거문도백도 은빛바다체험행사")
├── summary (축제 공통 overview: "축제 설명...")
├── imageUrl (축제 공통 firstimage: "https://..." / 없으면 firstimage2)
├── homepageUrl (축제 공통 homepage: "https://..." / 없으면 축제소개 eventhomepage)
├── category (축제 공통 lclsSystm3: "EV010100" → 저장 시 FestivalCategory 라벨로 변환하여 "문화관광" 등으로 저장, 매핑 안 되면 "기타")
├── phone (축제 공통 tel: "061-659-4742")
├── address (축제 공통 addr1: "전남 여수시...")
├── legalRegion (축제 공통 lDongRegnCd: "12" → Region.전남광주통합특별시)
├── legalGungu (legalRegion → RegionGroup.from(legalRegion): RegionGroup.JEONNAM)
├── location (축제 공통 mapx/mapy: "127.3099", "34.0244" → Point)
├── playTime (축제소개 playtime: "60분")
├── operationHours (축제소개 playtime: "18:00~22:00")
├── restDate (축제소개 restdate: "월요일" - 축제 담당자가 입력안하는 경우가 많음)
├── useFee (축제소개 usetimefestival: "무료")
├── parking (축제소개 parking: "가능" - 축제 담당자가 입력안하는 경우가 많음)
├── parkingFee (축제소개 parkingfee: "무료" - 축제 담당자가 입력안하는 경우가 많음)
├── eventStartDate (축제소개 eventstartdate: "20260731")
└── eventEndDate (축제소개 eventenddate: "20260801")
```

## TourSpot (tour_spots 테이블)

※ 축제 좌표를 기준으로 위치관광 API를 호출해 주변 관광지를 저장합니다.

```
├── contentId (위치관광 contentid: "123456")
├── title (위치관광 title: "무등산")
├── category (위치관광 lclsSystm1: "NA" → TourSpotCategory.NATURE)
├── imageUrl (위치관광 firstimage: "https://..." / 없으면 firstimage2)
├── address (위치관광 addr1: "광주광역시 동구...")
├── legalDongRegionCode (위치관광 lDongRegnCd: "24")
├── legalDongSigunguCode (위치관광 lDongSignguCd: "110")
├── phone (위치관광 tel: "062-000-0000")
├── apiModifiedAt (위치관광 modifiedtime: "20260721092317")
└── location (위치관광 mapx/mapy: "126.988", "35.135" → Point)
```

## 분류 체계 대분류 (lclsSystm1)

| 코드 | 이름 |
| --- | --- |
| AC | 숙박 |
| C01 | 추천코스 |
| EV | 축제/공연/행사 |
| EX | 체험관광 |
| FD | 음식 |
| HS | 역사관광 |
| LS | 레저스포츠 |
| NA | 자연관광 |
| SH | 쇼핑 |
| VE | 문화관광 |

## 분류 체계 중분류 (lclsSystm2)

※ 대분류(lclsSystm1)별 categoryCode2 조회 결과, 10개 대분류 전체 확보.

### AC 숙박 — 6개

| 코드 | 이름 |
| --- | --- |
| AC01 | 호텔 |
| AC02 | 콘도미니엄 |
| AC03 | 펜션/민박 |
| AC04 | 모텔 |
| AC05 | 캠핑 |
| AC06 | 호스텔 |

### C01 추천코스 — 6개

| 코드 | 이름 |
| --- | --- |
| C0112 | 가족코스 |
| C0113 | 나홀로코스 |
| C0114 | 힐링코스 |
| C0115 | 도보코스 |
| C0116 | 캠핑코스 |
| C0117 | 맛코스 |

### EV 축제/공연/행사 — 3개

| 코드 | 이름 |
| --- | --- |
| EV01 | 축제 |
| EV02 | 공연 |
| EV03 | 행사 |

### EX 체험관광 — 7개

| 코드 | 이름 |
| --- | --- |
| EX01 | 전통체험 |
| EX02 | 공예체험 |
| EX03 | 농.산.어촌 체험 |
| EX04 | 산사체험 |
| EX05 | 웰니스관광 |
| EX06 | 산업관광 |
| EX07 | 기타체험 |

### FD 음식 — 5개

| 코드 | 이름 |
| --- | --- |
| FD01 | 한식 |
| FD02 | 외국식 |
| FD03 | 간이음식 |
| FD04 | 주점 |
| FD05 | 카페/찻집 |

### HS 역사관광 — 4개

| 코드 | 이름 |
| --- | --- |
| HS01 | 역사유적지 |
| HS02 | 역사유물 |
| HS03 | 종교성지 |
| HS04 | 안보관광지 |

### LS 레저스포츠 — 4개

| 코드 | 이름 |
| --- | --- |
| LS01 | 육상레저스포츠 |
| LS02 | 수상레저스포츠 |
| LS03 | 항공레저스포츠 |
| LS04 | 복합레저스포츠 |

### NA 자연관광 — 5개

| 코드 | 이름 |
| --- | --- |
| NA01 | 자연경관(산) |
| NA02 | 자연경관(하천‧해양) |
| NA03 | 자연생태 |
| NA04 | 자연공원 |
| NA05 | 기타자연관광 |

### SH 쇼핑 — 7개

| 코드 | 이름 |
| --- | --- |
| SH01 | 백화점 |
| SH02 | 쇼핑몰 |
| SH03 | 대형마트 |
| SH04 | 면세점 |
| SH05 | 전문매장/상가 |
| SH06 | 시장 |
| SH07 | 기타쇼핑시설 |

### VE 문화관광 — 12개

| 코드 | 이름 |
| --- | --- |
| VE01 | 랜드마크관광 |
| VE02 | 테마공원 |
| VE03 | 도시공원 |
| VE04 | 도시.지역문화관광 |
| VE05 | 복합관광시설 |
| VE06 | 공연시설 |
| VE07 | 전시시설 |
| VE08 | 행사시설 |
| VE09 | 교육시설 |
| VE10 | 레저스포츠시설 |
| VE11 | 교통시설 |
| VE12 | 기타문화관광지 |

## EV01(축제) 소분류 (lclsSystm3)

※ TourAPI가 내려주는 lclsSystm3 코드는 저장 시 아래 표의 라벨로 변환되어 festivals.category 컬럼에 그대로 들어간다(예: "EV010100" → "문화관광"). 매핑되지 않는 코드는 "기타"로 저장된다.

| 코드 | 이름 |
| --- | --- |
| EV010100 | 문화관광 |
| EV010200 | 문화예술 |
| EV010300 | 지역특산물 |
| EV010400 | 전통역사 |
| EV010500 | 생태자연 |
| EV010600 | 기타 |

## 법정동 시도 코드

| 코드 | 시도명 |
| --- | --- |
| 11 | 서울특별시 |
| 12 | 전남광주통합특별시 |
| 26 | 부산광역시 |
| 27 | 대구광역시 |
| 28 | 인천광역시 |
| 30 | 대전광역시 |
| 31 | 울산광역시 |
| 41 | 경기도 |
| 43 | 충청북도 |
| 44 | 충청남도 |
| 47 | 경상북도 |
| 48 | 경상남도 |
| 50 | 제주특별자치도 |
| 51 | 강원특별자치도 |
| 52 | 전북특별자치도 |
| 36 | 세종특별자치시 |

## 지역별 탐색

| 권역 | 포함 시도 |
| --- | --- |
| 서울 | 서울특별시 |
| 경기·인천 | 경기도, 인천광역시 |
| 강원 | 강원특별자치도 |
| 충북 | 충청북도 |
| 충남권 | 충청남도, 대전광역시, 세종특별자치시 |
| 전북 | 전북특별자치도 |
| 전남권 | 전남광주통합특별시 |
| 경북권 | 경상북도, 대구광역시 |
| 경남권 | 경상남도, 부산광역시, 울산광역시 |
| 제주 | 제주특별자치도 |

## 지역 코드 (lDongSignguCd)

※ TourAPI `ldongCode2`(`lDongRegnCd` 파라미터로 조회) 실제 응답 기준입니다. 이전 버전의 "지역 코드" 트리(구/군 이름만 있고 코드가 없던 deprecated `areaCode2` 체계)를 대체합니다. 항목 없이 시도 자체가 곧 시군구인 경우(세종)는 시도 코드가 그대로 반복됩니다.

### 서울특별시 (11) — 25개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 종로구 |
| 140 | 중구 |
| 170 | 용산구 |
| 200 | 성동구 |
| 215 | 광진구 |
| 230 | 동대문구 |
| 260 | 중랑구 |
| 290 | 성북구 |
| 305 | 강북구 |
| 320 | 도봉구 |
| 350 | 노원구 |
| 380 | 은평구 |
| 410 | 서대문구 |
| 440 | 마포구 |
| 470 | 양천구 |
| 500 | 강서구 |
| 530 | 구로구 |
| 545 | 금천구 |
| 560 | 영등포구 |
| 590 | 동작구 |
| 620 | 관악구 |
| 650 | 서초구 |
| 680 | 강남구 |
| 710 | 송파구 |
| 740 | 강동구 |

### 인천광역시 (28) — 11개 전체 확보

※ 예전 문서의 "중구/동구" 표기와 다릅니다. 2023년 행정구역 개편으로 제물포구/영종구/서해구/검단구가 신설되면서 이름이 바뀐 상태입니다.

| 코드 | 이름 |
| --- | --- |
| 125 | 제물포구 |
| 155 | 영종구 |
| 177 | 미추홀구 |
| 185 | 연수구 |
| 200 | 남동구 |
| 237 | 부평구 |
| 245 | 계양구 |
| 275 | 서해구 |
| 290 | 검단구 |
| 710 | 강화군 |
| 720 | 옹진군 |

### 대전광역시 (30) — 5개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 동구 |
| 140 | 중구 |
| 170 | 서구 |
| 200 | 유성구 |
| 230 | 대덕구 |

### 대구광역시 (27) — 9개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 중구 |
| 140 | 동구 |
| 170 | 서구 |
| 200 | 남구 |
| 230 | 북구 |
| 260 | 수성구 |
| 290 | 달서구 |
| 710 | 달성군 |
| 720 | 군위군 |

### 전남광주통합특별시 (12) — 27개 전체 확보

※ 광주(구 단위)와 전남(시/군 단위)이 하나의 목록으로 합쳐져서 옵니다.

| 코드 | 이름 |
| --- | --- |
| 110 | 목포시 |
| 130 | 여수시 |
| 150 | 순천시 |
| 170 | 나주시 |
| 190 | 광양시 |
| 210 | 동구 (광주) |
| 240 | 서구 (광주) |
| 270 | 남구 (광주) |
| 300 | 북구 (광주) |
| 330 | 광산구 (광주) |
| 710 | 담양군 |
| 720 | 곡성군 |
| 730 | 구례군 |
| 740 | 고흥군 |
| 750 | 보성군 |
| 760 | 화순군 |
| 770 | 장흥군 |
| 780 | 강진군 |
| 790 | 해남군 |
| 800 | 영암군 |
| 810 | 무안군 |
| 820 | 함평군 |
| 830 | 영광군 |
| 840 | 장성군 |
| 850 | 완도군 |
| 860 | 진도군 |
| 870 | 신안군 |

### 부산광역시 (26) — 16개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 중구 |
| 140 | 서구 |
| 170 | 동구 |
| 200 | 영도구 |
| 230 | 부산진구 |
| 260 | 동래구 |
| 290 | 남구 |
| 320 | 북구 |
| 350 | 해운대구 |
| 380 | 사하구 |
| 410 | 금정구 |
| 440 | 강서구 |
| 470 | 연제구 |
| 500 | 수영구 |
| 530 | 사상구 |
| 710 | 기장군 |

### 울산광역시 (31) — 5개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 중구 |
| 140 | 남구 |
| 170 | 동구 |
| 200 | 북구 |
| 710 | 울주군 |

### 경기도 (41) — 55개 전체 확보

※ 시 이름과, 구가 있는 시는 그 하위 구까지 같이 나옵니다.

| 코드 | 이름 |
| --- | --- |
| 110 | 수원시 |
| 111 | 수원시 장안구 |
| 113 | 수원시 권선구 |
| 115 | 수원시 팔달구 |
| 117 | 수원시 영통구 |
| 130 | 성남시 |
| 131 | 성남시 수정구 |
| 133 | 성남시 중원구 |
| 135 | 성남시 분당구 |
| 150 | 의정부시 |
| 170 | 안양시 |
| 171 | 안양시 만안구 |
| 173 | 안양시 동안구 |
| 190 | 부천시 |
| 192 | 부천시 원미구 |
| 194 | 부천시 소사구 |
| 196 | 부천시 오정구 |
| 210 | 광명시 |
| 220 | 평택시 |
| 250 | 동두천시 |
| 270 | 안산시 |
| 271 | 안산시 상록구 |
| 273 | 안산시 단원구 |
| 280 | 고양시 |
| 281 | 고양시 덕양구 |
| 285 | 고양시 일산동구 |
| 287 | 고양시 일산서구 |
| 290 | 과천시 |
| 310 | 구리시 |
| 360 | 남양주시 |
| 370 | 오산시 |
| 390 | 시흥시 |
| 410 | 군포시 |
| 430 | 의왕시 |
| 450 | 하남시 |
| 460 | 용인시 |
| 461 | 용인시 처인구 |
| 463 | 용인시 기흥구 |
| 465 | 용인시 수지구 |
| 480 | 파주시 |
| 500 | 이천시 |
| 550 | 안성시 |
| 570 | 김포시 |
| 590 | 화성시 |
| 591 | 화성시 만세구 |
| 593 | 화성시 효행구 |
| 595 | 화성시 병점구 |
| 597 | 화성시 동탄구 |
| 610 | 광주시 |
| 630 | 양주시 |
| 650 | 포천시 |
| 670 | 여주시 |
| 800 | 연천군 |
| 820 | 가평군 |
| 830 | 양평군 |

### 강원특별자치도 (51) — 18개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 춘천시 |
| 130 | 원주시 |
| 150 | 강릉시 |
| 170 | 동해시 |
| 190 | 태백시 |
| 210 | 속초시 |
| 230 | 삼척시 |
| 720 | 홍천군 |
| 730 | 횡성군 |
| 750 | 영월군 |
| 760 | 평창군 |
| 770 | 정선군 |
| 780 | 철원군 |
| 790 | 화천군 |
| 800 | 양구군 |
| 810 | 인제군 |
| 820 | 고성군 |
| 830 | 양양군 |

### 충청북도 (43) — 15개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 청주시 |
| 111 | 청주시 상당구 |
| 112 | 청주시 서원구 |
| 113 | 청주시 흥덕구 |
| 114 | 청주시 청원구 |
| 130 | 충주시 |
| 150 | 제천시 |
| 720 | 보은군 |
| 730 | 옥천군 |
| 740 | 영동군 |
| 745 | 증평군 |
| 750 | 진천군 |
| 760 | 괴산군 |
| 770 | 음성군 |
| 800 | 단양군 |

### 충청남도 (44) — 17개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 130 | 천안시 |
| 131 | 천안시 동남구 |
| 133 | 천안시 서북구 |
| 150 | 공주시 |
| 180 | 보령시 |
| 200 | 아산시 |
| 210 | 서산시 |
| 230 | 논산시 |
| 250 | 계룡시 |
| 270 | 당진시 |
| 710 | 금산군 |
| 760 | 부여군 |
| 770 | 서천군 |
| 790 | 청양군 |
| 800 | 홍성군 |
| 810 | 예산군 |
| 825 | 태안군 |

### 경상북도 (47) — 24개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 포항시 |
| 111 | 포항시 남구 |
| 113 | 포항시 북구 |
| 130 | 경주시 |
| 150 | 김천시 |
| 170 | 안동시 |
| 190 | 구미시 |
| 210 | 영주시 |
| 230 | 영천시 |
| 250 | 상주시 |
| 280 | 문경시 |
| 290 | 경산시 |
| 730 | 의성군 |
| 750 | 청송군 |
| 760 | 영양군 |
| 770 | 영덕군 |
| 820 | 청도군 |
| 830 | 고령군 |
| 840 | 성주군 |
| 850 | 칠곡군 |
| 900 | 예천군 |
| 920 | 봉화군 |
| 930 | 울진군 |
| 940 | 울릉군 |

### 경상남도 (48) — 23개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 120 | 창원시 |
| 121 | 창원시 의창구 |
| 123 | 창원시 성산구 |
| 125 | 창원시 마산합포구 |
| 127 | 창원시 마산회원구 |
| 129 | 창원시 진해구 |
| 170 | 진주시 |
| 220 | 통영시 |
| 240 | 사천시 |
| 250 | 김해시 |
| 270 | 밀양시 |
| 310 | 거제시 |
| 330 | 양산시 |
| 720 | 의령군 |
| 730 | 함안군 |
| 740 | 창녕군 |
| 820 | 고성군 |
| 840 | 남해군 |
| 850 | 하동군 |
| 860 | 산청군 |
| 870 | 함양군 |
| 880 | 거창군 |
| 890 | 합천군 |

### 제주특별자치도 (50) — 2개 전체 확보

※ 예전 문서의 "남제주군/북제주군"은 더 이상 존재하지 않습니다. 지금은 제주시/서귀포시 2개뿐입니다.

| 코드 | 이름 |
| --- | --- |
| 110 | 제주시 |
| 130 | 서귀포시 |

### 전북특별자치도 (52) — 16개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 110 | 전주시 |
| 111 | 전주시 완산구 |
| 113 | 전주시 덕진구 |
| 130 | 군산시 |
| 140 | 익산시 |
| 180 | 정읍시 |
| 190 | 남원시 |
| 210 | 김제시 |
| 710 | 완주군 |
| 720 | 진안군 |
| 730 | 무주군 |
| 740 | 장수군 |
| 750 | 임실군 |
| 770 | 순창군 |
| 790 | 고창군 |
| 800 | 부안군 |

### 세종특별자치시 (36) — 1개 전체 확보

| 코드 | 이름 |
| --- | --- |
| 36110 | 세종특별자치시 |


### searchFestival2에서 얻은 contentid로 -> 

 ## /detailIntro2 반환값
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
  <response>
    <header>
      <resultCode>0000</resultCode>
      <resultMsg>OK</resultMsg>
    </header>
    <body>
      <items>
        <item>
          <contentid>2614762</contentid>
          <contenttypeid>15</contenttypeid>
          <sponsor1>거제해양관광개발공사</sponsor1>
          <sponsor1tel>055-639-8193</sponsor1tel>
          <sponsor2>
          </sponsor2>
          <sponsor2tel>
          </sponsor2tel>
          <eventenddate>20260912</eventenddate>
          <playtime>18:00~22:00</playtime>
          <eventplace>장승포 수변공원</eventplace>
          <eventhomepage>
          </eventhomepage>
          <agelimit>
          </agelimit>
          <bookingplace>
          </bookingplace>
          <placeinfo>
          </placeinfo>
          <subevent>
          </subevent>
          <program>1. 메인 공연 : 지역가수 및 전문팀 공연(EDM·댄스·7080·K-POP), 시민 참여 이벤트
2. 맥주존 : 생맥주 무제한 제공
3. 먹거리존 : 푸드트럭, 장승포동 주민자치회 먹거리 부스
4. 체험 프로그램 : 지역 막걸리 전시·홍보·시음 체험, 거제 관광 홍보부스
5. 부대행사 : 포토존, 외국인 통역 지원 부스, 베리어프리존, 무더위 쉼터, 친환경 캠페인

[이용요금]
- 1차 사전예매(8.7.~23.) 12,000원(20%할인)
- 2차 사전예매(8.24~9.10.) 13,500원(10%할인)</program>
  <eventstartdate>20260911</eventstartdate>
  <usetimefestival>유료</usetimefestival>
  <discountinfofestival>
  </discountinfofestival>
  <spendtimefestival>
  </spendtimefestival>
  <festivalgrade>
  </festivalgrade>
  <progresstype>선택안함</progresstype>
  <festivaltype>
  </festivaltype>
  </item>
  </items>
  <numOfRows>1</numOfRows>
  <pageNo>1</pageNo>
  <totalCount>1</totalCount>
    </body>
  </response>
```

## /detailCommon2
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
  <response>
    <header>
      <resultCode>0000</resultCode>
      <resultMsg>OK</resultMsg>
    </header>
    <body>
      <items>
        <item>
          <contentid>141105</contentid>
          <contenttypeid>15</contenttypeid>
          <title>경남고성공룡세계엑스포</title>
          <createdtime>20060414090000</createdtime>
          <modifiedtime>20260803143202</modifiedtime>
          <tel>055-670-7400</tel>
          <telname>경남 고성군</telname>
          <homepage>www.gngsctf.or.kr</homepage>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/32/4084832_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/32/4084832_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <lDongRegnCd>48</lDongRegnCd>
          <lDongSignguCd>820</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010600</lclsSystm3>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <addr1>경상남도 고성군 당항만로 1116</addr1>
          <addr2>당항포관광지</addr2>
          <zipcode>52915</zipcode>
          <mapx>128.3915143393</mapx>
          <mapy>35.0533072967</mapy>
          <mlevel>6</mlevel>
          <overview>경남고성공룡세계엑스포는 대한민국 경상남도 고성군 회화면 당항포관광지에서 개최되는 공룡을 주제로 한 축제이다. 고성군은 세계 3대 공룡발자국 화석 원산지로 널리 알려진 지역으로 여러 지역에서 발견된 크고 작은 공룡화석의 가치를 알리고 관광산업을 알리기 위하여 2006년부터 개최되어 왔다. 행사장인 당항포관광지는 공룡테마존과 이순신테마존으로 구성되어 있며, 해안산책로, 등산로, 공룡 발자국 탐방로 등의 야외 테마 공원을 갖추고 있다. 행사의 주최는 경상남도 고성군이며, 2026년 현재 (재)고성문화관광재단에서 경남고성공룡세계엑스포를 운영하고 있다. 2026년 엑스포는 10회째, 20주년을 맞았으며 &apos;공룡과 떠나는 신나는 모험&apos;을 주제로 개최된다. 또한, 매회 다양한 전시·체험 프로그램과 공연을 통해 국내 대표 공룡 테마 축제로 자리매김하고 있다.</overview>
        </item>
      </items>
      <numOfRows>1</numOfRows>
      <pageNo>1</pageNo>
      <totalCount>1</totalCount>
    </body>
  </response>

```

## /searchFestival2
```
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
  <response>
    <header>
      <resultCode>0000</resultCode>
      <resultMsg>OK</resultMsg>
    </header>
    <body>
      <items>
        <item>
          <addr1>서울특별시 강남구 도산대로 320 (논현동)</addr1>
          <addr2>
          </addr2>
          <zipcode>06054</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>737479</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20090521185913</createdtime>
          <eventstartdate>20261003</eventstartdate>
          <eventenddate>20261005</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/43/4101843_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/43/4101843_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>127.0369343307</mapx>
          <mapy>37.5221428648</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260826183754</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>02-3423-5543</tel>
          <title>강남페스티벌</title>
          <lDongRegnCd>11</lDongRegnCd>
          <lDongSignguCd>680</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010200</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>서울특별시 강동구 올림픽로 875 (암사동)</addr1>
          <addr2>
          </addr2>
          <zipcode>05239</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>1307813</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20110615014707</createdtime>
          <eventstartdate>20261016</eventstartdate>
          <eventenddate>20261018</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/77/3541977_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/77/3541977_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>127.13060065465167</mapx>
          <mapy>37.55906143476573</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260227174111</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>02-3425-5240</tel>
          <title>강동선사문화축제</title>
          <lDongRegnCd>11</lDongRegnCd>
          <lDongSignguCd>740</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010100</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>강원특별자치도 강릉시 창해로14번길 20-1 (견소동)</addr1>
          <addr2>
          </addr2>
          <zipcode>25556</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>825295</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20091022232428</createdtime>
          <eventstartdate>20261021</eventstartdate>
          <eventenddate>20261025</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/24/3546224_image2_1.JPG</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/24/3546224_image3_1.JPG</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>128.9473094259</mapx>
          <mapy>37.7726104945</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260618175538</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>033-647-6802</tel>
          <title>강릉커피축제</title>
          <lDongRegnCd>51</lDongRegnCd>
          <lDongSignguCd>150</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010100</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>경상남도 거제시 장승로 138 (장승포동)</addr1>
          <addr2>
          </addr2>
          <zipcode>53322</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>2614762</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20190809184709</createdtime>
          <eventstartdate>20260911</eventstartdate>
          <eventenddate>20260912</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/11/4097611_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/11/4097611_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>128.7245993248</mapx>
          <mapy>34.8663308815</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260825103422</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>055-639-8193</tel>
          <title>거제맥주축제</title>
          <lDongRegnCd>48</lDongRegnCd>
          <lDongSignguCd>310</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010200</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>경기도 수원시 권선구 세화로134번길 37 (서둔동)</addr1>
          <addr2>
          </addr2>
          <zipcode>16621</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>3368470</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20240913144625</createdtime>
          <eventstartdate>20261121</eventstartdate>
          <eventenddate>20261122</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/09/4106209_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/09/4106209_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>126.9971816138</mapx>
          <mapy>37.2668071001</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260831163507</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>031-774-3312</tel>
          <title>경기미 디저트 페스타</title>
          <lDongRegnCd>41</lDongRegnCd>
          <lDongSignguCd>113</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV03</lclsSystm2>
          <lclsSystm3>EV030200</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>경상남도 고성군 당항만로 1116</addr1>
          <addr2>당항포관광지</addr2>
          <zipcode>52915</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>141105</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20060414090000</createdtime>
          <eventstartdate>20260922</eventstartdate>
          <eventenddate>20261101</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/32/4084832_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/32/4084832_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>128.3915143393</mapx>
          <mapy>35.0533072967</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260803143202</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>055-670-7400</tel>
          <title>경남고성공룡세계엑스포</title>
          <lDongRegnCd>48</lDongRegnCd>
          <lDongSignguCd>820</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010600</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>서울특별시 종로구 사직로 161 (세종로)</addr1>
          <addr2>경복궁</addr2>
          <zipcode>03045</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>2648460</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20200224192834</createdtime>
          <eventstartdate>20260902</eventstartdate>
          <eventenddate>20261024</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/35/4100435_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/35/4100435_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>126.9767218661</mapx>
          <mapy>37.5760307000</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260819091218</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>1522-2295</tel>
          <title>경복궁 별빛야행</title>
          <lDongRegnCd>11</lDongRegnCd>
          <lDongSignguCd>110</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010200</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>경상북도 경산시 남매로 100 (상방동)</addr1>
          <addr2>경산생활체육공원 온마루광장(구 어귀마당)</addr2>
          <zipcode>38637</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>140897</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20050915090000</createdtime>
          <eventstartdate>20260919</eventstartdate>
          <eventenddate>20260920</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/14/4102114_image2_1.jpg</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/14/4102114_image3_1.jpg</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>128.7437733447</mapx>
          <mapy>35.8211171309</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260907132402</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>053-819-0334</tel>
          <title>경산 갓바위소원성취축제</title>
          <lDongRegnCd>47</lDongRegnCd>
          <lDongSignguCd>290</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010200</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>경상북도 경주시 인왕동 839-1</addr1>
          <addr2>
          </addr2>
          <zipcode>780150</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>2614760</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20190809184135</createdtime>
          <eventstartdate>20260918</eventstartdate>
          <eventenddate>20260920</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/20/4107120_image2_1.JPG</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/20/4107120_image3_1.JPG</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>129.2188486000</mapx>
          <mapy>35.8346809000</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260904112229</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>054-743-7182</tel>
          <title>경주 국가유산야행</title>
          <lDongRegnCd>47</lDongRegnCd>
          <lDongSignguCd>130</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010400</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
        <item>
          <addr1>충청남도 계룡시 신도안면 석계리 계룡대 활주로</addr1>
          <addr2>
          </addr2>
          <zipcode>32800</zipcode>
          <cat1>
          </cat1>
          <cat2>
          </cat2>
          <cat3>
          </cat3>
          <contentid>2992940</contentid>
          <contenttypeid>15</contenttypeid>
          <createdtime>20230707171713</createdtime>
          <eventstartdate>20261001</eventstartdate>
          <eventenddate>20261005</eventenddate>
          <firstimage>https://tong.visitkorea.or.kr/cms/resource/51/4106751_image2_1.JPG</firstimage>
          <firstimage2>https://tong.visitkorea.or.kr/cms/resource/51/4106751_image3_1.JPG</firstimage2>
          <cpyrhtDivCd>Type3</cpyrhtDivCd>
          <mapx>127.2366628000</mapx>
          <mapy>36.3083994000</mapy>
          <mlevel>6</mlevel>
          <modifiedtime>20260903170934</modifiedtime>
          <areacode>
          </areacode>
          <sigungucode>
          </sigungucode>
          <tel>042-840-2621~4</tel>
          <title>계룡軍문화축제</title>
          <lDongRegnCd>44</lDongRegnCd>
          <lDongSignguCd>250</lDongSignguCd>
          <lclsSystm1>EV</lclsSystm1>
          <lclsSystm2>EV01</lclsSystm2>
          <lclsSystm3>EV010100</lclsSystm3>
          <progresstype>선택안함</progresstype>
          <festivaltype>
          </festivaltype>
        </item>
      </items>
      <numOfRows>10</numOfRows>
      <pageNo>1</pageNo>
      <totalCount>259</totalCount>
    </body>
  </response>
  ```
