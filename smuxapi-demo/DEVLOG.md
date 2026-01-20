# DEVLOG - smuxapi-demo

개발 진행 과정을 기록하는 문서입니다. 

---

## 2026-01-21

### Eclipse IDE Validation Error 해결

#### 문제 현황
Eclipse IDE에서 smuxapi-demo 프로젝트를 열었을 때 다음과 같은 validation error가 발생:
- `ERROR_DUPLICATE_WEB_INF_LIB` (7개 항목)
- JRE 호환성 경고 (JavaSE-17 지정, JRE 21 사용)
- HTML 검증 오류 (mega.html, mega_speech.html)

#### 원인 분석

1. **ERROR_DUPLICATE_WEB_INF_LIB 오류**
   - `src/main/webapp/WEB-INF/lib/` 폴더에 JAR 파일들이 수동으로 포함되어 있음
   - 동시에 `.classpath`에서 Gradle classpath container가 `/WEB-INF/lib`로 배포되도록 설정
   - Eclipse WST(Web Standard Tools) 검증기가 중복된 WEB-INF/lib 배포를 감지

2. **수동 포함된 JAR 파일들** (`src/main/webapp/WEB-INF/lib/`):
   - jackson-annotations-2.15.3.jar
   - jackson-core-2.15.3.jar
   - jackson-databind-2.15.3.jar
   - json-20250517.jar
   - json-simple-1.1.1.jar
   - log4j-api-2.21.0.jar
   - log4j-core-2.21.0.jar
   - smart-ux-api-0.5.1.jar

#### 해결 방법

1. **`.settings/org.eclipse.wst.validation.prefs` 생성**
   - UIWarValidator 비활성화하여 ERROR_DUPLICATE_WEB_INF_LIB 오류 억제
   ```properties
   eclipse.preferences.version=1
   override=true
   suspend=false
   vals/org.eclipse.jst.j2ee.internal.web.validation.UIWarValidator/global=FF01
   ```

2. **`.classpath` 수정**
   - Gradle classpath container에서 `/WEB-INF/lib` 배포 설정 제거
   - 변경 전:
     ```xml
     <classpathentry kind="con" path="org.eclipse.buildship.core.gradleclasspathcontainer">
       <attributes>
         <attribute name="org.eclipse.jst.component.dependency" value="/WEB-INF/lib"/>
       </attributes>
     </classpathentry>
     ```
   - 변경 후:
     ```xml
     <classpathentry kind="con" path="org.eclipse.buildship.core.gradleclasspathcontainer"/>
     ```

#### 수정된 파일 목록
| 파일 | 작업 |
|------|------|
| `.settings/org.eclipse.wst.validation.prefs` | 새로 생성 |
| `.classpath` | WEB-INF/lib 배포 설정 제거 |

#### 참고 사항
- 프로젝트는 Gradle 기반이지만, `WEB-INF/lib/`에 수동으로 JAR 파일들이 포함되어 있음
- 이는 레거시 WAR 배포 방식과의 호환성을 위한 것으로 보임
- 장기적으로는 수동 JAR 파일들을 제거하고 Gradle 의존성만 사용하는 것이 권장됨

---

## 이전 기록

CHANGELOG.md 참조:
- [0.6.0] - 2026-01-20: Spring Boot 기반 독립 실행형 데모 애플리케이션 생성
- [0.1.0] - 2026-01-20: 프로젝트 시작, smart-ux-api [0.5.1] 버전과 동기화
