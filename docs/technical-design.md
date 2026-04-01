# Technical Design

## Chosen Direction

- Platform: Android native
- Language: Kotlin
- Minimum OS: Android 12
- UI split:
  - Activity onboarding: Jetpack Compose
  - Overlay runtime: `WindowManager` + custom `View`

## Why This Architecture

- オーバーレイ描画は `WindowManager` と通常 `View` の方がタッチ制御と描画制御を明示しやすい
- Activity 側は Compose の方が権限説明やステータス表示を短く保守しやすい
- 描画を永続化しないため、ローカル DB やファイル層は V1 では不要

## Current Implementation Status

- `MainActivity` は Compose で実装されており、権限状態表示、権限設定導線、overlay 開始・停止ボタンを持つ
- Overlay 実行中は `OverlayService` が Foreground Service として常駐する
- runtime UI は複数 window に分割されている
  - フローティングボタン
  - interactive な描画キャンバス
  - passive annotation 用キャンバス
  - 描画ツールパレット
  - compact な `Tools` チップ
- 描画データは `DrawingSessionStore` にメモリ保持し、終了時に破棄する
- 位置計算は `OverlayPositioning` に切り出され、単体テストで clamp / 左右端吸着を確認している
- `OverlayCanvasView` は確定済みストロークを bitmap キャッシュへ描き込み、入力中ストロークだけを前景で再描画する

## Commercialization Direction

- ファーストリリースは無料版のみを想定する
- V1 では Google Play Billing や entitlement 判定を必須依存にしない
- 将来の有料版に備える場合は、機能境界を UI と状態管理の両面で分離し、内部 flag で無効化できる構造を優先する
- 課金導線や購入状態の永続化は、需要確認と運用体制整理が済むまで実装しない

## Runtime Components

### `MainActivity`

- Overlay 権限状態の確認
- 権限設定画面への導線
- Overlay service の開始と停止
- 現在のプロトタイプ仕様を画面上で説明

### `OverlayService`

- Foreground Service として常駐
- 通知の表示
- フローティングボタン、描画キャンバス、ツールパレットの lifecycle 管理
- `Keep`、`Hide`、通知からの `Resume`、`Clear`、`Stop` を処理
- bubble と palette / chip の位置をセッション中だけ保持する

### `DrawingSessionStore`

- 現在のブラシ設定
- 描画ストローク一覧
- Undo
- リスナー通知

### `OverlayCanvasView`

- ストローク描画
- 入力中ストロークのプレビュー
- 消しゴムは `BlendMode.CLEAR` で実装
- 座標は正規化して保持し、画面サイズ変化に追従しやすくする
- 確定済みストロークは bitmap キャッシュへ描き込み、入力中だけを前面で再描画する

### `ToolPaletteView`

- ペン
- 消しゴム
- アンドゥ
- クリア
- 太さ
- 透明度
- 色
- ペン種別切り替え
- Keep と Close
- Move ハンドルによる再配置
- ストロークがないときは `Undo`、`Clear`、`Keep` を無効化
- `Hide` では interactive canvas を外し、passive overlay と compact な `Tools` チップへ切り替える

### `OverlayPositioning`

- 画面内に収まるよう overlay 座標を clamp する
- bubble と palette を左右端へ吸着させる
- `WindowManager` 依存を分離し、単体テストしやすい形で保持する

## Overlay States

1. Idle
   - フローティングボタンのみ
2. Drawing
   - 全画面キャンバスとツールパレットを表示
   - 下位アプリのタッチはブロック
3. Passive Annotation
   - 描画だけを残す
   - フローティングボタンを戻す
   - overlay window alpha は `0.78`
4. Stopped
   - すべての window と描画を破棄

## Implemented Behavior Notes

- Passive Annotation では overlay alpha を `0.78` に固定している
- `Hide` はセッション終了ではなく、描画入力を止めて `Tools` チップへ切り替える動作
- `Clear` はセッションを維持したままストロークだけを消去する
- 描画座標は正規化して保持するため、同一セッション中の view サイズ変化に追従しやすい
- persistence は未実装で、アプリ再起動をまたぐ復元は行わない

## Tradeoffs Accepted For V1

- 描画保存はしない
- 一部アプリでは overlay 非表示の可能性がある
- Passive Annotation では描画自体も 78 パーセント相当の見え方になる
- bubble とパネル位置はセッション内保持のみで、アプリ再起動をまたいでは保持しない
- 画面端吸着は左右のみで、上下方向は安全マージン付きの clamp に留める
- premium 候補機能を先行実装する場合も、公開版では無料体験を複雑化させない

## Validation Status

- `DrawingSessionStore` のストローク保持、色変更時のツール復帰、全消しは unit test 済み
- `OverlayPositioning` の clamp と左右端吸着は unit test 済み
- 実機での overlay 制約確認、OEM 差分確認、長時間稼働確認は未完了

## Next Technical Steps

1. 実機で Pixel と Galaxy の overlay 挙動を確認する
2. Android 12 から 15 の Passive Annotation 体験を記録し、仕様文言を固定する
3. 画面回転、マルチウィンドウ、再入場時の window 再構成を検証する
4. OEM ごとのバックグラウンド制限と overlay 非表示条件を整理する
