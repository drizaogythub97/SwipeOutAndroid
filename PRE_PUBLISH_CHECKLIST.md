# Checklist pré-publicação — SwipeOut Android

Use esta lista antes de rodar `git push` e tornar o repositório público.

---

## 🔴 CRÍTICO — resolver antes de publicar

- [ ] **`app/debug.keystore` está no repositório e sendo usado para assinar o build de release**
  - O arquivo `debug.keystore` está versionado em `app/debug.keystore`
  - O `app/build.gradle.kts` aponta esse arquivo como `signingConfig` do release
  - **O que fazer:** remova o keystore do histórico Git e nunca versione keystores de release
  - Para limpar do histórico: `git filter-repo --invert-paths --path app/debug.keystore`
    (ou `git filter-branch` — mas `filter-repo` é mais seguro)
  - Depois: adicione `*.keystore` e `*.jks` ao `.gitignore`
  - Para release, use variáveis de ambiente ou GitHub Actions Secrets para injetar as credenciais de assinatura no CI

- [ ] **`local.properties` — confirmar que não está no histórico**
  - Já está no `.gitignore` atual, mas verifique se foi commitado em algum momento:
    `git log --all -- local.properties`
  - Se aparecer: limpe com `git filter-repo` também
  - Este arquivo pode conter o caminho do SDK local, o que não é sensível, mas é desnecessário no repo

---

## 🟡 IMPORTANTE — recomendado antes de publicar

- [ ] **`.gitignore` — adicionar entradas que estão faltando**

  O `.gitignore` atual está básico. Adicione as linhas abaixo:
  ```gitignore
  # Keystores e assinatura
  *.keystore
  *.jks

  # Arquivos gerados pelo Gradle
  .gradle/
  build/
  app/build/

  # Android Studio
  .idea/
  *.iml

  # Local
  local.properties

  # macOS
  .DS_Store

  # Kotlin/KSP gerados
  app/src/main/java/com/swipeout/hilt_aggregated_deps/
  ```

- [ ] **Verificar se há strings hardcoded com dados pessoais**
  - Procure por emails, nomes ou URLs internas no código:
    `grep -r "adriano\|@gmail\|swipeout.app" app/src --include="*.kt"`

- [ ] **Verificar se `applicationId` é o desejado**
  - Atualmente: `com.swipeout`
  - Se for publicar na Play Store futuramente, considere um applicationId mais único (ex: `com.adrianocardoso.swipeout`)

- [ ] **Adicionar arquivo `LICENSE`**
  - O README menciona MIT mas o arquivo `LICENSE` não existe ainda
  - Crie em `LICENSE` com o texto MIT padrão (substitua `[YEAR]` e `[YOUR NAME]`):
    ```
    MIT License

    Copyright (c) 2026 Adriano Cardoso

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
    ```

---

## 🟢 COSMÉTICO — melhora a aparência do repo no GitHub

- [ ] **Adicionar screenshots/GIFs ao README**
  - Tire screenshots da HomeScreen, SwipeScreen (com overlay MANTER/DELETAR) e ReviewScreen
  - Para GIFs: grave o swipe em ação (Android Studio tem gravador de tela ou use `adb screenrecord`)
  - Coloque em `docs/` e atualize os placeholders no README

- [ ] **Configurar o repositório no GitHub**
  - Nome sugerido: `SwipeOut` ou `swipeout-android`
  - Descrição: `Tinder-style gallery cleaner for Android. Swipe to keep or delete your photos and videos, month by month.`
  - Topics/tags: `android`, `kotlin`, `jetpack-compose`, `room`, `hilt`, `media3`, `exoplayer`, `mvvm`, `gallery`, `clean-up`
  - Marcar como "Public"
  - Adicionar website se tiver (ou deixar em branco)

- [ ] **Fixar o repo no perfil do GitHub**
  - Vá em seu perfil → "Customize your profile" → pине o SwipeOut como um dos 6 repos em destaque

- [ ] **Verificar que o build passa em ambiente limpo**
  - Clone em outra pasta (ou use uma máquina diferente) e rode `./gradlew assembleDebug`
  - Confirma que não há dependência de arquivo local não versionado

---

## Resumo rápido de prioridades

| Prioridade | Item |
|------------|------|
| 🔴 Hoje | Remover `debug.keystore` do histórico |
| 🔴 Hoje | Nunca commitar keystore de release |
| 🟡 Antes do push | Atualizar `.gitignore` |
| 🟡 Antes do push | Criar arquivo `LICENSE` |
| 🟡 Antes do push | Verificar strings pessoais no código |
| 🟢 Depois | Screenshots/GIFs no README |
| 🟢 Depois | Configurar topics/tags no GitHub |
| 🟢 Depois | Fixar no perfil |
