# GitHub Pages セットアップ手順

最終更新日: 2026-04-01

このリポジトリでは、`docs/` を GitHub Pages の公開元にする想定です。

## 公開される主なページ

- ルート: `https://<GitHubユーザー名>.github.io/overlay-pen/`
- 日本語版プライバシーポリシー: `https://<GitHubユーザー名>.github.io/overlay-pen/privacy-policy/`
- 英語版プライバシーポリシー: `https://<GitHubユーザー名>.github.io/overlay-pen/privacy-policy/en/`

## GitHub 上での設定

1. GitHub で `overlay-pen` リポジトリを開く
2. `Settings` を開く
3. 左メニューの `Pages` を開く
4. `Build and deployment` の `Source` で `Deploy from a branch` を選ぶ
5. `Branch` で `main` を選ぶ
6. フォルダで `/docs` を選ぶ
7. `Save` を押す

初回は数分待つと公開されます。

## 公開確認

Pages の画面に公開 URL が表示されます。  
以下の 3 ページが開ければ設定は完了です。

- `/overlay-pen/`
- `/overlay-pen/privacy-policy/`
- `/overlay-pen/privacy-policy/en/`

## カスタムドメインを使う場合

独自ドメインを使う場合は、GitHub Pages 側で `Custom domain` を設定し、DNS レコードも追加します。  
公開後は HTTPS を有効にし、可能ならドメイン検証も行ってください。

## 公開前に差し替える項目

以下のプレースホルダは公開前に実値へ差し替えてください。

- `docs/privacy-policy.md`
- `docs/privacy-policy.en.md`
- `docs/privacy-policy/index.html`
- `docs/privacy-policy/en/index.html`

差し替え対象:

- 公開用メールアドレス
- 実際の公開 URL
