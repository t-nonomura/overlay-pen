# PROJECT

## Mission

他アプリの利用中でも即座にメモやマーキングができる、Android 向けの軽量オーバーレイ落書きツールを立ち上げる。

## Current Phase

- 現在は Discovery 完了後の Prototype 実装・検証フェーズ
- 基本フローの Android 実装は着手済みで、主要 overlay UI はリポジトリ内に存在する
- 次の焦点は、実機検証、OEM 差分確認、Alpha に向けた品質調整

## Team Operation

このプロジェクトは今後も 5 ロールのチームとして運用する前提とします。以後の仕様検討、実装提案、リスク整理では、次の観点を明示的に扱います。

- Product Owner: リリース範囲、無料版と将来有料版の線引き、公開優先順位
- Android Lead: 実装容易性、保守性、feature flag や課金導入余地を含む設計
- UX Designer: 無料版としての分かりやすさ、将来アップセル時の自然な体験
- QA Lead: 無料版の品質基準、隠し機能が無料体験へ悪影響を与えないか
- Security and Policy Owner: Google Play 公開情報、権限説明、課金導入時のポリシー負荷

## Product Statement

- 誰のため: 画面上で説明、記録、指示、検討メモを残したいユーザー
- 何を解決するか: スクリーンショット編集や別アプリへの遷移なしで、その場で画面上に書き込みたい
- どう差別化するか: フローティング起動、最小ステップ、描画の一時保持、消しゴムとアンドゥを含む即時編集

## Team Setup

初期フェーズは 5 ロールの少人数チームを想定します。1 人が複数ロールを兼務しても構いません。

| Role | Main Responsibility | Initial Deliverables |
| --- | --- | --- |
| Product Owner | スコープ確定、優先順位、受け入れ判定 | 要件承認、V1 外スコープ整理 |
| Android Lead | Overlay 実装方式、Service、権限、描画性能 | 技術スパイク、アーキテクチャ、実装方針 |
| UX Designer | フローティング UI、描画ツールバー、初回権限導線 | 画面遷移、状態設計、操作ルール |
| QA Lead | 端末マトリクス、OS 差分、再現手順、回帰試験 | テスト観点、受け入れ試験、端末別課題 |
| Security and Policy Owner | Google Play ポリシー、権限説明、公開可否判断 | 公開リスク整理、説明文、ストア申請論点 |

## Working Agreements

- 要件変更は `docs/v1-requirements.md` に反映してから実装へ進む
- 権限や公開ポリシーに関わる判断は必ず `docs/logs/` に記録する
- V1 では「実現できること」よりも「安定して説明できること」を優先する
- Android 標準 API で成立しない要件は、早期に仕様変更候補としてエスカレーションする
- 実装の現状と docs の記述がズレた場合は、コード変更と同じタイミングで docs も更新する
- 無料版のスコープ判断は Product Owner 主導、課金や公開情報の判断は Security and Policy Owner を必須参加とする
- 将来の有料版候補を先行実装する場合でも、無料版の主導線、品質、審査説明を複雑化させない

## Decision Model

- プロダクト仕様: Product Owner が最終決定
- 技術方式: Android Lead が提案し、Security and Policy Owner と合議
- 公開可否: Product Owner と Security and Policy Owner が共同判断

## Milestone Status

1. Discovery
   - 完了
   - 要件定義、実現性調査、公開リスク整理を docs に反映済み
2. Prototype
   - 進行中
   - フローティングボタン、キャンバス、表示専用 overlay、描画ツール、通知操作まで実装済み
3. Technical Validation
   - これから強化
   - Android 12 から 15、Pixel / Galaxy / Xiaomi 系で overlay 挙動を実機確認する
4. Alpha
   - 未着手
   - 端末テスト、描画最適化、権限説明導線、クラッシュ耐性を固める
5. Beta / Release Decision
   - 未着手
   - 公開判断、ストア説明、品質安定化を行う

## Recommended Backlog Ownership

- Product Owner
  - ユースケース優先度
  - V1 と V2 の線引き
  - 無料版で観測したい市場シグナルの定義
- Android Lead
  - OverlayService
  - WindowManager 制御
  - 描画エンジン
  - feature flag / entitlement 導入余地の設計
- UX Designer
  - ボタン位置調整
  - ツールバー情報設計
  - 権限オンボーディング
  - 将来プレミアム導線の情報設計
- QA Lead
  - 端末別挙動
  - 長時間起動
  - 画面回転、マルチウィンドウ
  - 非公開機能が無料版へ混入しないことの確認
- Security and Policy Owner
  - `SYSTEM_ALERT_WINDOW`
  - Foreground Service 宣言
  - Play listing 文言
  - 課金導入時の公開情報とアカウント移行条件

## Release and Monetization Policy

- ファーストリリースは無料版のみを公開する
- 公開初期は市場需要の確認を優先し、アプリ内課金や有料解除は行わない
- 将来の有料版は、基幹機能の需要が確認できた場合に限り検討する
- 有料版の公開を判断した場合は、個人アカウント運用を前提にせず、組織アカウント移行または新規組織アカウントへのアプリ移管と、公開住所の運用方法を確認してから進める
- リッチ機能を先行実装する場合は、feature flag や内部設定で閉じ、無料版ユーザーから見える導線は設けない
- 無料版の KPI と有料版判断基準は別途ログに残し、場当たりで課金導入を決めない

## Free Release Completion Criteria By Role

### Product Owner

- 無料版の価値提案を「他アプリ上にすぐ描ける軽量オーバーレイ注釈」に固定できている
- ファーストリリースの対象機能と V2 以降の対象機能が明文化されている
- 公開後に観測したい市場シグナルを定義している
  - 継続利用
  - レビュー内容
  - 要望頻度
  - 問い合わせ内容

### Android Lead

- 開始、描画、保持、非表示、再開、停止の主導線が Android 12 以上で安定している
- 無料版の機能境界がコード上で整理され、将来 premium 候補を分離しやすい
- Billing 依存、購入状態管理、課金関連 SDK を含めない
- 非公開機能がある場合も、無効時に主導線へ影響しない

### UX Designer

- 権限許可、描画開始、描画終了、再開が迷わず行える
- 無料版の UI に未解放機能や期待だけを煽る導線が出ていない
- 権限説明と overlay 制約の伝え方が最低限成立している

### QA Lead

- 主要端末と OS 範囲で基本フローの受け入れ試験が通っている
- 長時間起動、再入場、通知操作、画面回転で致命的な不具合がない
- 非公開 premium 候補が無料版ビルドへ誤露出しないことを確認できている

### Security and Policy Owner

- `SYSTEM_ALERT_WINDOW` と Foreground Service の説明がストア文面として整理されている
- 無料版が収益化アプリとして解釈される導線を持たない
- サポート連絡先、公開情報、配布方針が現行アカウント運用に整合している

## Premium Candidate Evaluation Framework

将来 premium 候補を検討する際は、次の 5 ロール視点で評価する。

- Product Owner
  - その機能が需要検証後の売り物として独立価値を持つか
  - 無料版の価値提案を壊さず差別化できるか
- Android Lead
  - 現行設計から無理なく分離できるか
  - 常時有効でなくても保守負債が増えにくいか
- UX Designer
  - 無料版に不自然な欠損感を出さず premium 化できるか
  - 将来の導線が押し売りに見えないか
- QA Lead
  - 無効時と有効時の両方を継続的に検証できるか
  - 端末差分や描画性能へ悪影響を与えないか
- Security and Policy Owner
  - 課金導入時に公開情報、利用規約、サポート負荷が許容範囲か
  - 無料版時点で誤認を招く要素を持ち込まないか

## Current Exit Criteria

- Android 12 以上で、開始、描画、保持、非表示、停止の基本導線が安定動作する
- Passive Annotation の見え方とタッチスルー制約を実機で説明可能な状態にする
- 画面回転、再入場、通知操作、長時間起動の振る舞いを整理する
- V1 の配布方針を Google Play 先行か限定配布先行かで判断できる
