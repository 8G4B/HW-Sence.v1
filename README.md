# HW-Sence
Windows와 Linux 환경에서 하드웨어 상태를 웹페이지로 확인하는 프로그램이에요!
Ktor와 Kotlin을 사용해서 만들었어요.

[개발자 프로필 보기](https://github.com/snowykte0426)

## 사용법
Windows와 Linux,그리고 그안에서도 Docker를 이용하는 방법과 직접 구동하는 방식으로 나뉘어요<br>
Local에서 구동하였다면 [http://localhost:18029](http://localhost:18029)로 접속하면 됩니다
### Windows
#### Docker를 이용하는 방법
1. Docker Desktop을 설치해주세요<br>
  [다운로드 링크](https://www.docker.com/products/docker-desktop)
2. Git을 설치해주세요<br>
  [다운로드 링크](https://git-scm.com/download/win)
3. PowerShell을 실행해주세요
4. 해당 레포지터리를 Clone해주세요
```shell
git clone https://github.com/8G4B/HW-Sence.git
```
5. Clone한 폴더로 이동해주세요
```shell
cd HW-Sence
```
6. Docker를 이용해 빌드해주세요
```shell
docker build -t hw-sence .
```
7. Docker를 이용해 실행해주세요
```shell
docker run -p 18029:18029 hw-sence
```
#### 직접 구동하는 방법
1. Git을 설치해주세요<br>
  [다운로드 링크](https://git-scm.com/download/win)
2. PowerShell을 실행해주세요
3. 해당 레포지터리를 Clone해주세요
```shell
git clone https://github.com/8G4B/HW-Sence.git
```
4. Clone한 폴더로 이동해주세요
```shell
cd HW-Sence
```
5. Gradle을 이용해 빌드해주세요
```shell
gradlew build
```
6. Gradle을 이용해 실행해주세요
```shell
gradlew run
```
### Linux
#### Docker를 이용하는 방법
1. Docker를 설치해주세요
```shell
sudo apt-get install docker.io
```
2. Git을 설치해주세요
```shell
sudo apt-get install git
```
3. 해당 레포지터리를 Clone해주세요
```shell
git clone https://github.com/8G4B/HW-Sence.git
```
4. Clone한 폴더로 이동해주세요
```shell
cd HW-Sence
```
5. Docker를 이용해 빌드해주세요
```shell
docker build -t hw-sence .
```
6. Docker를 이용해 실행해주세요
```shell
docker run -p 18029:18029 hw-sence
```
#### 직접 구동하는 방법
1. Git을 설치해주세요
```shell
sudo apt-get install git
```
2. 해당 레포지터리를 Clone해주세요
```shell
git clone https://github.com/8G4B/HW-Sence.git
```
3. Clone한 폴더로 이동해주세요
```shell
cd HW-Sence
```
4. Gradle을 이용해 빌드해주세요
```shell
./gradlew build
```
5. Gradle을 이용해 실행해주세요
```shell
./gradlew run
```
## 기능
- CPU 사용량
- 메모리 사용량
- 디스크 사용량
- 상위 5개 프로세스