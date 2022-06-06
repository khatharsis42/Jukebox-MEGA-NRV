# Jukebox-MEGA-NRV
Ce projet est une tentative de recoder le [jukebox](https://github.com/khatharsis42/jukebox-ultra-nrv), en Kotlin, en utilisant Ktor. Le terme "MEGA" pour remplacer le "ULTRA" a été choisi entierement abritrairement.

## Fonctionnalitées
### Musiques et playlist
N'importe quel utilisateur peut rajouter une musique venant de plusieurs sources différentes à la playlist "En Cours" de l'application. L'utilisateur est également averti des musiques actuellement dans la playlist grâce à l'interface Web.
### Base de donnée
L'application possède une base de donnée qui garde une trace non seulement de tous les utilisateurs, mais également des musiques ajoutées, et de chaque ajout individuel (appellé un Log, correspondant à la lecture d'une musique ajoutée par un utilisateur à un instant). Cette base de donnée permet notamment de faire des statistiques sur les utilisateurs (chose très appréciée par ces derniers), mais également de recommander des musiques déjà ajoutées de manière aléatoire aux utilisateurs, fonctionnalité très pratique pour découvrire de nouvelles musiques.
### Lecture des musiques
La lecture des musiques (**Qui n'est pas encore implémentée**) se basera sur [MPV](https://github.com/mpv-player/mpv/blob/master/DOCS/man/libmpv.rst), et devra permettre de jouer, pauser, avancer et reculer des musiques.
### Youtube-DL
Contrairement à l'ancien Jukebox, celui çi utilise entièrement [yt-dlp](https://github.com/yt-dlp/yt-dlp).

## Licensing
This project uses Fontawesome icons, which underare Creative Commons Attribution 4.0
International license. The complete license can be found [here](https://fontawesome.com/license).
