# Overlay Pen

Android 向けのオーバーレイ落書きツールです。

ユーザーはフローティングボタンから落書きモードを起動し、他アプリの上に透明キャンバスを重ねて手書きできます。落書きモードを抜けた後も描画は表示されたままとし、ツール自体を終了したときに全描画を消去する仕様です。

現時点のステータスは、Discovery を完了し、Android プロトタイプ実装とドキュメント更新を並行して進めている段階です。リポジトリには要件定義と技術検討に加え、Overlay の基本フローを確認できるアプリ実装が含まれています。

## Current Status

- `app/` に Android アプリのプロトタイプ実装が存在する
- `MainActivity` で Overlay 権限状態の確認、開始、停止ができる
- `OverlayService` で Foreground Service と overlay window 群を管理している
- フローティングボタン、描画キャンバス、表示専用 overlay、ツールパネル、compact な `Tools` チップが動作する
- 描画はセッション中メモリ保持のみで、終了時に消去する
- `DrawingSessionStore` と `OverlayPositioning` には単体テストがある
- 実機での Android 12 から 15 の挙動確認、OEM 差分確認、公開方針整理は継続課題

## Implemented Prototype

- フローティングボタン表示とドラッグ移動
- 全画面透明キャンバスでの連続描画
- ペン、消しゴム、アンドゥ、リドゥ、全消し
- 太さ、透明度、色、ペン種類の変更
- 描画パネルのドラッグ移動と左右端吸着
- `Hide` で compact な `Tools` チップへ切り替え
- 通知からの再開、全消し、停止

## Known Gaps

- 実機での Android 12 から 15 の検証結果はまだドキュメント化途中
- 一部アプリでの `HIDE_OVERLAY_WINDOWS` 挙動は未整理
- 権限オンボーディングは最小実装で、説明 UX は今後改善余地がある
- Google Play 公開を前提にした説明文、審査観点、配布方針は未確定

## Goals

- オーバーレイ型の落書き体験が Android 上で成立するかを検証する
- V1 の要件と非要件を明文化して実装スコープを固定する
- Android 権限、Foreground Service、Google Play 公開のリスクを先に洗い出す

## Release Strategy

- ファーストリリースは最小限機能の無料版として公開する
- 初回公開では有料機能、アプリ内課金、課金導線を提供しない
- まずは基幹機能の需要、継続利用、ストア反応を観測し、需要が確認できた場合にのみ有料版を検討する
- 有料版を検討する段階では、個人デベロッパアカウントから組織運用への移行可否、新規組織アカウントへのアプリ移管要否、公開住所、サポート体制を改めて判断する
- 将来の有料版候補機能は内部実装や feature flag の形で先行着手してもよいが、無料版ではユーザー導線を開かない

## Current Direction

- Platform: Android first
- Tech direction: Native Android, Kotlin, View based custom canvas
- Delivery style: Discovery -> technical spike -> prototype -> alpha -> beta
- Recommended V1 OS range: Android 12 to Android 15

## Repository Structure

- `app/`: Android アプリ本体
- `docs/PROJECT.md`: プロジェクト体制、役割、運用
- `docs/v1-requirements.md`: V1 要件定義
- `docs/technical-design.md`: 実装方針とアーキテクチャ
- `docs/feasibility-study.md`: 実現性調査と技術リスク
- `docs/logs/2026-03-31-kickoff.md`: 初回キックオフの判断ログ
- `app/src/test/`: セッション状態と overlay 位置計算の単体テスト

## Build Notes

- Android Studio もしくは Android SDK + Gradle 環境を前提とする
- Java 17 を使用する
- Minimum OS は Android 12、`minSdk = 31`

## Important Caveat

`TYPE_APPLICATION_OVERLAY` を使う通常のオーバーレイ方式では、Android 12 以降のタッチ遮蔽制約により、描画を表示したまま完全に下のアプリへ自然にタッチを通す要件に制約があります。詳細は `docs/feasibility-study.md` を参照してください。

## Current Prototype Decision

- 対応 OS は Android 12 以上
- Passive Annotation は下位アプリの操作を優先し、window alpha を `0.78` に固定
- 描画データはサービスのメモリ上だけに保持し、ツール終了時に全消去
- 描画パネルはドラッグ移動でき、同一セッション中は位置を保持する
- セッションを終了せずに全ストロークを消去できる `Clear` 導線を持つ
- bubble と compact な chip は画面内で自由に移動でき、描画パネルは左右端へ吸着する
- `Hide` では描画入力を止めて compact な `Tools` ボタンへ切り替え、元アプリの操作を再開できる
