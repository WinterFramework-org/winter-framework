# 개발 가이드

## 로컬 Git hook 설정

[각 hook](../.githooks/)은 `<hook>.d/` 바로 아래의 파일을 이름순으로 실행하며, 실패하면 중단합니다.
작업 추가 시 해당 디렉터리에 실행 가능한 스크립트를 두세요. 실행 조건은 스크립트를 참고하세요.

새로 clone한 환경마다 저장소 루트에서 한 번 활성화합니다.
이 설정은 기존 hook 경로를 대체하므로, 기존 로컬 설정이 있다면 값을 기록하고 해제할 때 복원하세요.

### 활성화 및 확인

```bash
# 기존 로컬 설정 확인 (출력이 없으면 미설정)
git config --local --get core.hooksPath

# 활성화
git config --local core.hooksPath .githooks

# 적용 확인 (출력: .githooks)
git config --get core.hooksPath
```

### 해제 및 복원

기존 로컬 설정이 없었다면 삭제합니다. 삭제 후에는 전역 설정 등이 적용될 수 있습니다.

```bash
git config --local --unset core.hooksPath
```

기존 로컬 설정이 있었다면 아래 경로를 기록한 값으로 바꿔 실행합니다.

```bash
git config --local core.hooksPath '기존 경로'
```
