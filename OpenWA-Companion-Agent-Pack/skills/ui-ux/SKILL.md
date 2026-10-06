# Skill: UI/UX

## Objective
Bangun pengalaman yang sangat familiar dengan WhatsApp Android terbaru tanpa memakai logo, nama, atau asset proprietary.

## Method
1. Refresh UI berdasarkan official Android listing/blog/release screenshots yang tersedia saat build.
2. Gunakan repo UI clone berlisensi kompatibel hanya sebagai reference/component source.
3. Pisahkan hasil audit:
   - copied/adapted code
   - design reference only
   - original project code
4. Rebuild components in Jetpack Compose bila source reference menggunakan Flutter.
5. Jangan copy pixel/asset proprietary secara buta.

## Visual direction
- light/white clean
- neutral greys
- restrained green/teal accent milik OpenWA
- simple buttons
- subtle dividers
- minimal shadows
- professional typography
- responsive spacing
- no glassmorphism
- no blue-purple gradient
- no excessive rounded cards

## Interaction quality
Wajib:
- swipe to reply
- long press message actions
- selection mode
- scroll-to-bottom
- unread separator
- date separators
- media progress
- delivery/read indicators
- typing/presence indicator
- pull-to-refresh hanya jika masuk akal
- accessibility labels
- 48dp minimum touch targets
- dynamic font scaling
