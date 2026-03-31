# Overlay Pen

Android 向けのオーバーレイ落書きツール企画です。

ユーザーはフローティングボタンから落書きモードを起動し、他アプリの上に透明キャンバスを重ねて手書きできます。落書きモードを抜けた後も描画は表示されたままとし、ツール自体を終了したときに全描画を消去する構想です。

現時点のステータスは実装前の discovery フェーズです。今回の成果物は、要件定義、チーム体制、技術実現性、公開リスクの整理です。

## Goals

- オーバーレイ型の落書き体験が Android 上で成立するかを検証する
- V1 の要件と非要件を明文化して実装スコープを固定する
- Android 権限、Foreground Service、Google Play 公開のリスクを先に洗い出す

## Current Direction

- Platform: Android first
- Tech direction: Native Android, Kotlin, View based custom canvas
- Delivery style: Discovery -> technical spike -> prototype -> alpha -> beta
- Recommended V1 OS range: Android 12 to Android 15

## Repository Structure

- `docs/PROJECT.md`: プロジェクト体制、役割、運用
- `docs/v1-requirements.md`: V1 要件定義
- `docs/technical-design.md`: 実装方針とアーキテクチャ
- `docs/feasibility-study.md`: 実現性調査と技術リスク
- `docs/logs/2026-03-31-kickoff.md`: 初回キックオフの判断ログ

## Important Caveat

`TYPE_APPLICATION_OVERLAY` を使う通常のオーバーレイ方式では、Android 12 以降のタッチ遮蔽制約により、描画を表示したまま完全に下のアプリへ自然にタッチを通す要件に制約があります。詳細は `docs/feasibility-study.md` を参照してください。

## Current Prototype Decision

- 対応 OS は Android 12 以上
- Passive Annotation は下位アプリの操作を優先し、window alpha を `0.78` に固定
- 描画データはサービスのメモリ上だけに保持し、ツール終了時に全消去
- 描画パネルはドラッグ移動でき、同一セッション中は位置を保持する
- セッションを終了せずに全ストロークを消去できる `Clear` 導線を持つ
- bubble と描画パネルはドラッグ中に画面内へ制限され、指を離すと左右端へ吸着する
- `Hide` では描画入力を止めて compact な `Tools` ボタンへ切り替え、元アプリの操作を再開できる
