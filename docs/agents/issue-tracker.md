# Issue tracker: GitHub

이슈와 명세·작업 티켓은 WinterFramework-org/winter-framework의
GitHub Issues로 관리한다. gh CLI를 사용한다.

- 조회: gh issue view <번호> --repo WinterFramework-org/winter-framework
- 목록: gh issue list --repo WinterFramework-org/winter-framework
- 생성: 본문을 임시 파일에 작성하고 gh issue create의 --body-file로 전달한다.
- 댓글: 본문을 임시 파일에 작성하고 gh issue comment의 --body-file로 전달한다.
- 상태 라벨은 docs/agents/triage-labels.md의 매핑을 따른다.
- 작업 간 차단 관계는 GitHub의 native issue dependencies로 기록한다.
  지원되지 않으면 티켓 본문에 Blocked by: #<번호>로 기록한다.

스킬의 publish to the issue tracker는 GitHub 이슈 생성을 뜻한다.
fetch the relevant ticket은 해당 GitHub 이슈 조회를 뜻한다.

PRs as a request surface: no.
