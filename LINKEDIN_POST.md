# Post LinkedIn — SwipeOut

> **Formato:** parágrafo de abertura + bullets técnicos
> **Tom:** profissional mas autêntico, voltado para recrutadores e devs Android
> **Tamanho:** otimizado para LinkedIn (primeiras 3 linhas visíveis sem "ver mais")

---

## Versão PT-BR

---

Lancei meu primeiro app Android open source: SwipeOut — um limpador de galeria com interface de swipe estilo Tinder. 📱

A premissa é simples: você revisa suas fotos e vídeos mês a mês, desliza para manter ou deletar, e ao final um único dialog do sistema confirma tudo. Nada de anúncios, nada de conta, 100% offline.

O que me motivou a construir do zero em **Kotlin Nativo** em vez de Flutter ou React Native foi justamente poder trabalhar diretamente com as APIs do sistema — MediaStore, permissões adaptativas por versão de OS, MediaStore.createDeleteRequest. Sem camada de abstração no meio.

**Algumas decisões técnicas que gostei de tomar:**

• **Física de swipe sem duração fixa** — usei `AnchoredDraggableState` + `exponentialDecay` do Compose. O card desacelera de acordo com a velocidade real do gesto, não com um `durationMillis` arbitrário. A diferença no tato é perceptível.

• **ExoPlayer só onde precisa** — o card ativo aloca um player real; os outros dois usam `MediaMetadataRetriever` para extrair o primeiro frame. Vídeo roda durante arrasto e animações via um único `graphicsLayer` — solução que levou algumas iterações para acertar.

• **Undo ilimitado com pilha real** — cada swipe empilha a imagem e a direção; undo reverte a decisão no Room e anima o card de volta ao centro. Sem limite de histórico.

• **Room migration sem destruir dados** — a v3 do schema adicionou `bucketId`/`bucketName` para filtro por álbum. A migration faz backfill incremental via ContentResolver, preservando todas as decisões já tomadas.

• **Stack completa:** Kotlin 2.0 · Jetpack Compose · Hilt · Room · Coil 3 · Media3 · DataStore · Navigation Compose

O repositório está em [link do GitHub] com README completo, diagrama de arquitetura e instruções para rodar localmente.

Se você é dev Android e quer trocar ideia sobre qualquer uma dessas decisões, manda mensagem — adoraria o feedback. 🙌

#Android #Kotlin #JetpackCompose #OpenSource #AndroidDev #MobileEngineering

---

## Versão EN (opcional, para alcance maior)

---

I just open-sourced SwipeOut — a Tinder-style gallery cleaner for Android. 📱

The premise is simple: review your photos and videos month by month, swipe to keep or delete, and a single system dialog confirms all deletions at once. No ads, no account, fully offline.

I chose **native Kotlin** over cross-platform specifically to work directly with system APIs — MediaStore, version-aware permissions, and `MediaStore.createDeleteRequest`. No abstraction layer in between.

**Technical decisions I'm proud of:**

• **Swipe physics without fixed duration** — `AnchoredDraggableState` + `exponentialDecay` from Compose Foundation. The card decelerates based on actual gesture velocity, not an arbitrary `durationMillis`. The tactile difference is real.

• **ExoPlayer only where needed** — the top card allocates a real player; the other two use `MediaMetadataRetriever` for a static first frame. Video plays correctly through drag and animations via a single shared `graphicsLayer`.

• **Unlimited undo with a real stack** — every swipe pushes image + direction onto a history stack; undo pops it, reverts the Room decision, and animates the card back to center.

• **Non-destructive Room migration** — schema v3 added `bucketId`/`bucketName` for album filtering. The migration does an incremental ContentResolver backfill instead of wiping the table — all existing user decisions are preserved.

• **Full stack:** Kotlin 2.0 · Jetpack Compose · Hilt · Room · Coil 3 · Media3 · DataStore · Navigation Compose

Repo: [GitHub link] — full README, architecture diagram, and local setup instructions included.

If you're an Android dev and want to chat about any of these decisions, reach out — happy to get feedback. 🙌

#Android #Kotlin #JetpackCompose #OpenSource #AndroidDev #MobileEngineering

---

## Notas de uso

- **Primeira linha visível no LinkedIn** (sem clicar em "ver mais"): as 3 primeiras linhas do post. A versão PT-BR começa com "Lancei meu primeiro app Android open source" — isso aparece inteiro antes do corte.
- **Substituir `[link do GitHub]`** pelo URL real do repositório antes de publicar.
- **Imagem/GIF:** LinkedIn posts com mídia têm alcance significativamente maior. Recomendo anexar pelo menos um GIF do swipe em ação ou um screenshot da SwipeScreen com o overlay MANTER/DELETAR.
- **Horário de postagem:** terças a quintas entre 9h–11h ou 17h–18h (horário de Brasília) tendem a ter melhor engajamento.
- **Hashtags:** LinkedIn limita o boosting orgânico após muitas hashtags — 5 a 7 é o ideal. As sugeridas acima já estão dentro do limite.
