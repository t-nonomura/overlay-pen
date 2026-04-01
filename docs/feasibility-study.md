# Feasibility Study

## Executive Summary

結論として、このアプリは Android ネイティブで実装可能です。ただし、要件のうち「落書き終了後に描画を画面最前面へ残したまま、下の他アプリを自然に操作し続ける」は Android 12 以降で重要な制約があります。

推奨判断は以下です。

- 開発着手: Go
- 方式: Kotlin + Foreground Service + `TYPE_APPLICATION_OVERLAY`
- 公開形態: まずは限定配布または内部テストを推奨
- 仕様上の注意: 受け入れ条件に Android 制約を明記する

## Current Repository Status

- Kotlin / Android のプロトタイプ実装がリポジトリに存在する
- `TYPE_APPLICATION_OVERLAY`、Foreground Service、フローティングボタン、描画キャンバス、表示専用 overlay、ツールパレット、通知操作までコード化済み
- Passive Annotation の暫定仕様として、overlay alpha `0.78` を採用している
- ただし、これは机上判断と実装上の初期決定であり、Android 12 から 15 の実機マトリクス検証はまだ必要

## Proposed Technical Architecture

### Main Components

- `MainActivity`
  - 初回説明
  - 権限導線
  - セッション開始と終了
- `OverlayService`
  - Foreground Service
  - Window の追加と削除
  - セッション状態管理
- `FloatingLauncherView`
  - 起動用フローティングボタン
- `DrawingOverlayView`
  - 透明キャンバス
  - Path 管理
  - Undo 用ストローク履歴
- `ToolPaletteView`
  - ペン、消しゴム、太さ、色、透明度、種類

### Overlay State Model

1. Idle
   - フローティングボタンのみ表示
2. Drawing
   - 全画面キャンバスとツールバーを表示
   - 下のアプリのタッチはブロック
3. Passive Annotation
   - 描画だけを残し、編集 UI は隠す
   - フローティングボタンは表示維持
4. Stopped
   - すべての overlay と描画状態を破棄

## Current Prototype Decision

現行コードベースでは次の仕様を採用しています。

- 対応 OS は Android 12 以上
- Passive Annotation 用 overlay window alpha は `0.78`
- 描画データはメモリ保持のみで、停止時に全消去
- 描画中は interactive canvas を前面表示し、下位アプリ操作をブロック
- `Hide` では描画入力を止め、compact な `Tools` チップへ切り替える
- bubble と palette / chip はドラッグ移動でき、画面内 clamp と左右端吸着を行う

## Requirement Feasibility Rating

| Item | Rating | Notes |
| --- | --- | --- |
| フローティングボタン表示 | High | `SYSTEM_ALERT_WINDOW` と `TYPE_APPLICATION_OVERLAY` で実装可能 |
| 落書きモード表示 | High | 透明 `View` とカスタム描画で実装可能 |
| 消しゴム、アンドゥ、設定 UI | High | ローカル状態管理で実装可能 |
| 描画を残したまま元アプリ操作に戻る | Medium | Android 12 以降の untrusted touch 制約により仕様調整が必要 |
| ツール終了で描画を消去 | High | Service 終了時にメモリ破棄で対応可能 |
| Google Play 一般公開 | Medium | 特別権限と FGS の説明責任が重い |

## Key Platform Constraints

### 1. Overlay Permission Is Mandatory

`TYPE_APPLICATION_OVERLAY` を使うには `SYSTEM_ALERT_WINDOW` が必要です。API 23 以降はユーザーが設定画面で明示許可する必要があります。権限確認は `Settings.canDrawOverlays()`、誘導は `ACTION_MANAGE_OVERLAY_PERMISSION` を使います。

Android 11 以降、この intent はアプリ個別画面ではなくトップレベルの設定画面へ遷移します。つまり初回導線はやや重く、権限許可率に影響します。

### 2. Overlay Is Not Truly Above Everything

`TYPE_APPLICATION_OVERLAY` はアクティビティ上には表示されますが、ステータスバーや IME より上には出ません。したがって「画面全域に完全に描ける」とは限らず、キーボード表示中の一部領域などは期待どおりにならない可能性があります。

### 3. Android 12+ Blocks Unsafe Touch Pass-Through

Android 12 以降、`TYPE_APPLICATION_OVERLAY` は trusted window ではありません。`FLAG_NOT_TOUCHABLE` などで下のアプリへタッチを通そうとしても、overlay の不透明度条件を満たさないとシステムがタッチをブロックします。公式には、タッチ通過が許可されるのは overlay の `LayoutParams.alpha` が最大遮蔽不透明度以下であるなどの条件を満たす場合です。Android 12 での既定値は 0.8 です。

このため、V1 の最大論点は次の二択になります。

- 案 A: Passive Annotation 時の overlay 全体 alpha を 0.8 以下に制限し、タッチスルーを優先する
- 案 B: 描画をより不透明に保つ代わりに、描画がある領域で下のアプリ操作が阻害される可能性を受け入れる

### 4. Some Apps Can Hide Third-Party Overlays

Android 12 では、他アプリが `HIDE_OVERLAY_WINDOWS` と `setHideOverlayWindows()` を使うと、`TYPE_APPLICATION_OVERLAY` をそのアプリ上で隠せます。決済、認証、金融系などのセンシティブ画面では overlay が表示されないケースを想定すべきです。

つまり「すべてのアプリ上で常に落書きを残せる」は保証できません。

### 5. Foreground Service Is Practically Required

セッション維持とプロセス生存率の観点から Foreground Service を前提にするのが妥当です。Android 12 以降はバックグラウンドからの Foreground Service 起動に制限があり、Android 15 以降は `SYSTEM_ALERT_WINDOW` に加えて「現在 visible overlay があること」が exemption 条件に含まれます。

したがって V1 では、ユーザーが可視 Activity 上でツールを開始し、その直後に Service を foreground 化する設計が安全です。

## Alternatives Considered

### Alternative A: AccessibilityService Based Overlay

Accessibility overlay は trusted 扱いのため、タッチスルー面では有利です。ただし、Accessibility API は Play policy と説明責任が重く、本アプリの主目的が障害支援ではない限り、より狭い API を優先すべきです。

このため V1 の主要方式としては非推奨です。

### Alternative B: Screenshot Markup

スクリーンショット取得後に編集する方式なら OS 制約は軽くなりますが、「今見ている他アプリの上にそのまま描く」という中核価値を満たしません。

### Alternative C: MediaProjection Assisted Overlay

MediaProjection を使っても、今回必要なのは画面取得ではなく overlay 描画です。権限と実装負荷だけ増えるため V1 の利点は小さいです。

## Feasibility Decision By Requirement

### Fully Feasible

- フローティングボタン
- フルスクリーン透明キャンバス
- ペン、消しゴム、色、太さ、透明度、種類
- アンドゥ
- セッション終了時の全消去

### Feasible With Product Constraint

- 描画を残したまま下のアプリへ戻る
  - 追加条件が必要
  - タッチスルーか不透明度のどちらかを制限する必要がある

### Not Guaranteed Across All Apps

- 金融、認証、セキュア画面の上での常時表示
- ステータスバーや IME をまたぐ完全な最前面描画

## Publication Risk

### Google Play

`SYSTEM_ALERT_WINDOW` は special permission です。Google Play は restricted permissions を、アプリの core functionality に必要な場合だけ要求するよう求めています。今回のアプリは overlay 自体が主機能なので論理上は整合しますが、権限理由の説明と listing 上の明示が重要です。

Foreground Service についても、Android Developers は manifest 宣言が Google Play Device and Network Abuse policy に適合する必要があると明記しています。公開時は FGS の用途説明、ユーザー起点性、継続可視性を整理する必要があります。

### Monetization Timing

無料版のみで公開する方針は、初期リリースの運用負荷と公開情報リスクを抑えるうえで妥当です。Google Play の公式ヘルプでは、個人アカウントでも monetization 自体は可能ですが、in-app purchases や paid apps を扱う場合は physical address の登録と公開表示が必要とされています。

したがって、課金導入の是非は単なる機能優先順位ではなく、次の運用判断とセットで扱う必要があります。

- 公開住所をどう運用するか
- 組織アカウントへ移行するか、必要に応じて新規組織アカウントへアプリ移管するか
- 問い合わせ窓口やサポート導線をどう保守するか
- 有料版の売上見込みが、追加運用コストに見合うか

無料版で市場需要を確認し、需要が明確になった段階で組織運用へ寄せる方針は合理的です。

### Recommended Launch Path

1. 内部テスト
2. クローズドテスト
3. Google Play 一般公開可否を再判定

企業内配布や限定配布の方が V1 のリスクは低いです。

## Recommended V1 Scope Adjustment

V1 では次を明示的に仕様へ入れることを推奨します。

- Passive Annotation 中は描画表示の見え方に OS 制約がある
- 一部アプリや画面では overlay が非表示または操作制限される
- 描画はセッション内メモリ保持のみで、終了時に消去する

## Technical Spike Plan

### Spike 1: Passive Annotation Validation

- 状態: 未完了
- 目的: Android 12 から 15 でタッチスルー条件を確認する
- 判定: `LayoutParams.alpha` 0.8 以下で UX が許容できるか

### Spike 2: Overlay Window Composition

- 状態: 一部実装済み、検証未完了
- 目的: フローティングボタン、キャンバス、ツールバーを分離したときの combined opacity と操作性を確認する
- 判定: 複数 overlay の重なりで untrusted touch が増えないか

### Spike 3: Sensitive App Compatibility

- 状態: 未着手
- 目的: `HIDE_OVERLAY_WINDOWS` を使うアプリ上での挙動確認
- 判定: 非表示時のユーザー通知や fallback を定義できるか

### Spike 4: OEM Device Matrix

- 状態: 未着手
- 対象例:
  - Pixel
  - Samsung Galaxy
  - Xiaomi or Redmi
- 判定: メーカー別の自動起動、バックグラウンド制限、表示差異を把握できるか

## Final Recommendation

この企画は成立します。ただし、仕様をそのまま文字どおり実現するのではなく、Android 12 以降の overlay 制約を前提に V1 を再定義する必要があります。

最も安全な前進案は次のとおりです。

1. `TYPE_APPLICATION_OVERLAY` を主方式にする
2. V1 対応 OS を Android 12 以上に絞る
3. Passive Annotation の仕様を technical spike の結果で固定する
4. Google Play 一般公開は prototype 検証後に判断する

## Sources

- Android Developers: `Manifest.permission.SYSTEM_ALERT_WINDOW`
  - https://developer.android.com/reference/android/Manifest.permission
- Android Developers: `Settings.canDrawOverlays()`
  - https://developer.android.com/reference/android/provider/Settings.html
- Android Developers: `TYPE_APPLICATION_OVERLAY`
  - https://developer.android.com/reference/android/view/WindowManager.LayoutParams
- Android Developers: Android 11 permission updates
  - https://developer.android.com/about/versions/11/privacy/permissions
- Android Developers: Android 12 behavior changes for untrusted touches
  - https://developer.android.com/about/versions/12/behavior-changes-all
- Android Developers: Android 12 feature overview for `HIDE_OVERLAY_WINDOWS`
  - https://developer.android.com/about/versions/12/features
- Android Developers: Fraud prevention and `HIDE_OVERLAY_WINDOWS`
  - https://developer.android.com/security/fraud-prevention/activities
- Android Developers: Foreground Service declaration guidance
  - https://developer.android.com/develop/background-work/services/fgs/declare
- Android Developers: Foreground Service background start restrictions
  - https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start
- Google Play Console Help: Permissions and APIs that Access Sensitive Information
  - https://support.google.com/googleplay/android-developer/answer/16558241
