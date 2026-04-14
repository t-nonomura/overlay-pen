# AGENT.md

このリポジトリを別の PC から再開するときは、まずこのファイルを読むこと。  
このファイルは「最初に見る 1 枚」として運用し、現在の状態、重要ファイル、ローカル専用情報、直近タスクを集約する。

## 1. プロジェクト概要

- プロダクト名: `最前面ペン`
- 英語名候補: `Overlay Pen` / `Screen Pen`
- 概要: Android 12 以上で使える、画面に重ねて描ける注釈ツール
- 主用途:
  - 画面共有時の説明
  - 操作案内
  - 指差し、囲み、強調

## 2. 現在のフェーズ

- 現在は `クローズドテスト / 公開準備フェーズ`
- 基本機能の実装は揃っている
- 直近の重点:
  - 実機確認
  - Play Console 入力の確定
  - Foreground Service 審査対応
  - 公開前の文言と素材調整

## 3. 現在の機能状態

- フローティングボタンから overlay を開始できる
- 全画面 canvas に描画できる
- 対応ツール:
  - ペン
  - 消しゴム
  - アンドゥ
  - リドゥ
  - 全消し
- 調整可能項目:
  - 色
  - 太さ
  - 透明度
  - 線種
- 線種:
  - 通常
  - 蛍光
  - 破線
- 基本色:
  - 黒
  - 白
  - 赤
  - 青
  - 黄
- `Hide` 後は compact な `Tools` チップへ移行できる
- 最小化チップ状態でも `ON / OFF` ボタンで描画モード切り替えができる
  - `描く` 側を押すとフルパレットを開く
  - `ON` で最小化状態のまま描画
  - `OFF` で passive annotation に戻る

## 4. 現在の仕様上の重要点

- 対象 OS は Android 12 以上
- `TYPE_APPLICATION_OVERLAY` を使用
- `SYSTEM_ALERT_WINDOW` が必須
- `FOREGROUND_SERVICE` と `FOREGROUND_SERVICE_SPECIAL_USE` を使用
- Passive Annotation では overlay alpha を `0.78` に固定
  - 理由: Android 12+ の touch pass-through 制約対応
- 一部アプリは `HIDE_OVERLAY_WINDOWS` により overlay を隠す可能性がある

## 5. 無料版と premium 方針

- ファーストリリースは無料版のみ
- V1 では Billing や購入 UI を入れない
- premium 候補は将来判断
- 現時点の premium 候補:
  - 追加カラーパレット
  - 追加ペン種類
  - 描画の保存 / 復元
  - スクリーンショット保存
  - 共有機能

## 6. 最初に確認すべきファイル

この順に見ればだいたい把握できる。

1. `AGENT.md`
2. `README.md`
3. `docs/PROJECT.md`
4. `docs/v1-requirements.md`
5. `docs/technical-design.md`
6. `docs/feasibility-study.md`
7. `docs/logs/2026-04-03-release-prep-handoff.md`

## 7. 実装の主要ファイル

- ホーム画面:
  - `app/src/main/java/dev/overlaypen/app/MainActivity.kt`
- overlay 常駐管理:
  - `app/src/main/java/dev/overlaypen/app/overlay/OverlayService.kt`
- 描画状態:
  - `app/src/main/java/dev/overlaypen/app/overlay/DrawingSessionStore.kt`
- canvas 描画:
  - `app/src/main/java/dev/overlaypen/app/overlay/OverlayCanvasView.kt`
- ツール UI:
  - `app/src/main/java/dev/overlaypen/app/overlay/ToolPaletteView.kt`
- モデル:
  - `app/src/main/java/dev/overlaypen/app/model/DrawingModels.kt`
- 配置ロジック:
  - `app/src/main/java/dev/overlaypen/app/overlay/OverlayPositioning.kt`

## 8. ストア掲載用素材

- アプリアイコン PNG:
  - `store-assets/google-play/icon-512.png`
- フィーチャーグラフィック:
  - `store-assets/google-play/feature-graphic-1024x500.png`
- 再生成スクリプト:
  - `scripts/export-store-icon.ps1`
  - `scripts/export-feature-graphic.ps1`

## 9. プライバシーポリシー関連

本体リポジトリには草案と Pages 用 HTML がある。

- `docs/privacy-policy.md`
- `docs/privacy-policy.en.md`
- `docs/index.html`
- `docs/privacy-policy/index.html`
- `docs/privacy-policy/en/index.html`
- `docs/github-pages-setup.md`

方針:

- `overlay-pen` 本体 repo は private 運用
- privacy policy は別の public repo + GitHub Pages で公開する想定

## 10. ビルドと署名

### 通常ビルド

- debug build:
  - `.\gradlew.bat assembleDebug`
- release bundle:
  - `.\gradlew.bat bundleRelease`

### 現在のバージョン

- `versionCode = 2`
- `versionName = 0.1.0`

### 署名設定

- `app/build.gradle.kts` は `keystore.properties` がある場合だけ release signing を使う
- `.gitignore` で以下を除外済み:
  - `keystore.properties`
  - `*.jks`
  - `*.keystore`

### 生成物

- 提出用 AAB:
  - `app/build/outputs/bundle/release/app-release.aab`

## 11. ローカル専用情報

以下は git に入っていない。別 PC では別途復元が必要。

- `keystore/upload-keystore.jks`
- `keystore.properties`
- `local.properties`
- `privacy-policy-public/`

注意:

- `upload-keystore.jks` とパスワードは今後の更新配信に必要
- 別 PC で release build する場合は keystore を安全な方法で復元すること

## 12. Foreground Service 審査メモ

Play Console で求められる説明の要点:

- ユーザーが `オーバーレイ開始` を押した直後に描画 UI を即時表示する
- 他アプリ上で描画、待機、再表示を継続するため foreground service を使う
- 一時停止や再起動が入ると overlay 消失や描画セッション中断が起きる

審査用動画の想定内容:

1. アプリ起動
2. `オーバーレイ開始`
3. 通知表示確認
4. 他アプリ上にフローティング UI 表示
5. 実際に描画
6. `Hide`
7. 再表示または compact 描画
8. 終了

## 13. 直近タスク

優先度が高い順。

1. Play Console の入力欄を最終確定
2. クローズドテスト配布を継続
3. Foreground Service 審査用動画を撮影
4. privacy policy 公開用 public repo を整備
5. upload key の保管方法を改善
6. OEM / 実機差分の確認を継続

## 14. 再開時チェックリスト

別 PC で再開するときは以下を順に確認する。

1. この `AGENT.md` を読む
2. `git pull` して最新を取得
3. `README.md` と `docs/PROJECT.md` を確認
4. `local.properties` が無い場合は SDK パスを設定
5. release build が必要なら keystore を復元
6. `.\gradlew.bat assembleDebug` でまずビルド確認
7. 必要に応じて `.\gradlew.bat bundleRelease`

## 15. 更新ルール

このファイルは「最初に見る 1 枚」として維持する。

以下のどれかが変わったら更新すること。

- フェーズが変わった
- 公開方針が変わった
- 主要機能が増減した
- ストア素材や提出物が変わった
- ローカル専用情報の扱いが変わった
- 直近タスクの優先順位が変わった

詳細な経緯は `docs/logs/` に残し、このファイルには「今どうなっているか」を書く。
