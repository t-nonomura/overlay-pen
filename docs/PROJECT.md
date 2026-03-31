# PROJECT

## Mission

他アプリの利用中でも即座にメモやマーキングができる、Android 向けの軽量オーバーレイ落書きツールを立ち上げる。

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

## Decision Model

- プロダクト仕様: Product Owner が最終決定
- 技術方式: Android Lead が提案し、Security and Policy Owner と合議
- 公開可否: Product Owner と Security and Policy Owner が共同判断

## Initial Milestones

1. Discovery 完了
   - 要件定義、実現性調査、公開リスク整理
2. Technical Spike
   - Android 12 から 15 で overlay 挙動を実機検証
3. Prototype
   - フローティングボタン、キャンバス、消しゴム、アンドゥ
4. Alpha
   - 端末テスト、描画最適化、権限説明導線
5. Beta
   - 公開判断、ストア説明、品質安定化

## Recommended Backlog Ownership

- Product Owner
  - ユースケース優先度
  - V1 と V2 の線引き
- Android Lead
  - OverlayService
  - WindowManager 制御
  - 描画エンジン
- UX Designer
  - ボタン位置調整
  - ツールバー情報設計
  - 権限オンボーディング
- QA Lead
  - 端末別挙動
  - 長時間起動
  - 画面回転、マルチウィンドウ
- Security and Policy Owner
  - `SYSTEM_ALERT_WINDOW`
  - Foreground Service 宣言
  - Play listing 文言

## Exit Criteria For Discovery

- 実装方式を 1 つに絞れている
- 主要制約を踏まえた仕様変更案を持っている
- V1 公開形態を Google Play と配布限定のどちらかで判断できる
