# What this app is built from, and who owns it

## The typeface

**Atkinson Hyperlegible**, version 1.006, by the Braille Institute of America.

- **Files** `app/src/main/res/font/atkinson_hyperlegible_regular.ttf` and
  `atkinson_hyperlegible_bold.ttf`
- **Source** <https://github.com/googlefonts/atkinson-hyperlegible>, `fonts/ttf/`
- **Copyright** Copyright 2020 Braille Institute of America, Inc.
- **Licence** SIL Open Font License, Version 1.1, read from the `OFL.txt` in the
  same archive as the fonts and shipped inside the APK at `app/src/main/assets/OFL-AtkinsonHyperlegible.txt`

The OFL permits bundling the font inside an application and redistributing it,
including commercially, provided the licence travels with the font files and the
Reserved Font Name is not used for a modified version. Neither file here is
modified: both are the upstream binaries, byte for byte, renamed only because
Android resource names may not contain capitals or hyphens.

Why this face rather than any other is argued in
`app/src/main/java/com/simplemode/firetv/ui/theme/Theme.kt`. The short version:
the Braille Institute commissioned it to be read by people with low vision, and
this app claims to be an interface for someone who cannot read her television.

# Where the photographs came from

Every photograph in Simple Mode is from Wikimedia Commons under a licence that
permits redistribution and commercial use. Nothing here is from a streaming
service: a subscription permits watching, not republishing key art.

The licence, the author and the file page below were read from the Commons API
on the same request that returned each image, by `tools/fetch_tile_photos.py`,
which rejects anything carrying an NC or ND term before it is ever downloaded.
They are not transcribed from a landing page.

Each image is cropped to 16:9, graded to its tile's dominant tone and given a
gradient scrim along the bottom so the label clears it. The recipe is
`tools/build_photos.py` and the picks are in `tools/chosen_photographs.json`;
the originals are unmodified on Commons.

Most of these are CC BY-SA. Cropping and grading makes an adaptation, and
share-alike applies to adaptations, so **the modified images under
`res/drawable-*/art_*.png` are offered under CC BY-SA 4.0**. That covers the
pictures, not the app: the Kotlin, the layout and the palette are the
project's own and carry the repository's licence. Anything that needs a
picture free of that obligation should replace it with a CC0 or public-domain
frame; `tools/build_photos.py` regenerates the whole set from one table.

Checked 2026-09-23.

## Tell Me tile

- **File** `res/drawable-*/art_tell_me.png`
- **Source** [File:Carmen 2.JPG](https://commons.wikimedia.org/wiki/File:Carmen_2.JPG)
- **Author** Vorans
- **Licence** CC BY-SA 4.0

## Live TV tile

- **File** `res/drawable-*/art_live_tv.png`
- **Source** [File:Aerial antenna.JPG](https://commons.wikimedia.org/wiki/File:Aerial_antenna.JPG)
- **Author** Daniel Christensen
- **Licence** CC BY 3.0

Replaced 2026-09-25. The photograph this tile carried before was a radio
museum piece — not a television at all — and
an internal design review's item 3 named it directly as the kind of
literal, generic stock ("a television for Live TV") the set's other imagery
had already moved past. A rooftop aerial reads as broadcast reception without
being a photograph of a screen, which is also the one tile picture in this
app that isn't, in some sense, a screen showing a screen.

## Her Shows tile

- **File** `res/drawable-*/art_her_shows.png`
- **Source** [File:Chair and Lamp (Non-Flashed Version).jpg](https://commons.wikimedia.org/wiki/File:Chair_and_Lamp_(Non-Flashed_Version).jpg)
- **Author** Deavmi
- **Licence** CC BY-SA 3.0

## Films tile

- **File** `res/drawable-*/art_films.png`
- **Source** [File:Brea-edwards cinema night.jpg](https://commons.wikimedia.org/wiki/File:Brea-edwards_cinema_night.jpg)
- **Author** Jonnyboyca at English Wikipedia
- **Licence** Public domain

Replaced 2026-09-25. The auditorium-interior photograph this tile carried
before is the other image an internal design review's item 3 named
directly ("a cinema for Films") — legible, but the generic default rather than
a choice. This is a real marquee's own neon, cropped and scrimmed so the
specific showtimes on the boards lower in the source frame (a real
multiplex's real listings, not this catalogue's) fall inside this pipeline's
own label scrim and are dark before they would ever read as somebody else's
programme.

## Box Sets tile

- **File** `res/drawable-*/art_box_sets.png`
- **Source** [File:35mm movie negative.jpg](https://commons.wikimedia.org/wiki/File:35mm_movie_negative.jpg)
- **Author** Runner1616
- **Licence** CC BY-SA 3.0

## Family Videos tile

- **File** `res/drawable-*/art_family_videos.png`
- **Source** [File:Hungarian family photo album, page 44.jpg](https://commons.wikimedia.org/wiki/File:Hungarian_family_photo_album,_page_44.jpg)
- **Author** Unknown authorUnknown author
- **Licence** Public domain

## Call for Help screen

- **File** `res/drawable-*/art_call_for_help.png`
- **Source** [File:Radio-Depot der Technischen Sammlungen Dresden (Rundfunk- und Fernsehempfänger) 14.jpg](https://commons.wikimedia.org/wiki/File:Radio-Depot_der_Technischen_Sammlungen_Dresden_(Rundfunk-_und_Fernsehempf%C3%A4nger)_14.jpg)
- **Author** Eremeev
- **Licence** CC BY-SA 4.0

## Captions tile, when a caregiver puts one on the wall

- **File** `res/drawable-*/art_captions.png`
- **Source** [File:TSD-Fernseher-RAFENA-kol.jpg](https://commons.wikimedia.org/wiki/File:TSD-Fernseher-RAFENA-kol.jpg)
- **Author** Kolossos
- **Licence** CC BY-SA 3.0

## Fallback picture for any app a caregiver adds later

- **File** `res/drawable-*/art_another_app.png`
- **Source** [File:Columbia City Cinema main hall.jpg](https://commons.wikimedia.org/wiki/File:Columbia_City_Cinema_main_hall.jpg)
- **Author** Joe Mabel
- **Licence** CC BY-SA 3.0

## The header photograph, deleted

There used to be a graded photograph of a Victorian living room behind the title
on the home screen and on every inner screen. It is gone, along with
`res/drawable-*/art_hero_room.png` -- 2.5 MB of APK that carried no information
and made this app the same shape as the other two Fire TV apps in this
repository. See an internal cleanup log for this app's screens. The Commons original, by Jorge Royan under
CC BY-SA 3.0, is untouched where it always was.

## News channel tile

- **File** `res/drawable-*/art_ch_news.png`
- **Source** [File:Truman Media Studio with News 12 Set.jpg](https://commons.wikimedia.org/wiki/File:Truman_Media_Studio_with_News_12_Set.jpg)
- **Author** David L Roush
- **Licence** CC BY-SA 3.0

## Weather channel tile

- **File** `res/drawable-*/art_ch_weather.png`
- **Source** [File:Colorful sky with orange clouds reflecting in the water of a paddy field, at sunset, Vang Vieng, Laos.jpg](https://commons.wikimedia.org/wiki/File:Colorful_sky_with_orange_clouds_reflecting_in_the_water_of_a_paddy_field,_at_sunset,_Vang_Vieng,_Laos.jpg)
- **Author** Basile Morin
- **Licence** CC BY-SA 4.0

## Classic cinema channel tile

- **File** `res/drawable-*/art_ch_classic.png`
- **Source** [File:Zeiss Ikon Ernemann II film projector, 1930s - Tekniska museet - Stockholm, Sweden - DSC01562.JPG](https://commons.wikimedia.org/wiki/File:Zeiss_Ikon_Ernemann_II_film_projector,_1930s_-_Tekniska_museet_-_Stockholm,_Sweden_-_DSC01562.JPG)
- **Author** Daderot
- **Licence** CC0

## Game shows channel tile

- **File** `res/drawable-*/art_ch_gameshows.png`
- **Source** [File:Wien - Staatsoper, Zuschauerraum mit Bühne.JPG](https://commons.wikimedia.org/wiki/File:Wien_-_Staatsoper,_Zuschauerraum_mit_B%C3%BChne.JPG)
- **Author** C.Stadler/Bwag
- **Licence** CC BY-SA 4.0

## "Sunday" show tile

- **File** `res/drawable-*/art_show_sunday.png`
- **Source** [File:Cozy living room with tropical patterned armchair and natural light from large windows.jpg](https://commons.wikimedia.org/wiki/File:Cozy_living_room_with_tropical_patterned_armchair_and_natural_light_from_large_windows.jpg)
- **Author** Shixart1985
- **Licence** CC BY 2.0

## Detective show tile

- **File** `res/drawable-*/art_show_detective.png`
- **Source** [File:DSCF1209 A quiet dimly lit alley at night with a lone parked car near the glow of a streetlamp flanked by high walls and buildings.jpg](https://commons.wikimedia.org/wiki/File:DSCF1209_A_quiet_dimly_lit_alley_at_night_with_a_lone_parked_car_near_the_glow_of_a_streetlamp_flanked_by_high_walls_and_buildings.jpg)
- **Author** PattayaPatrol
- **Licence** CC BY-SA 4.0

## Baking show tile

- **File** `res/drawable-*/art_show_baking.png`
- **Source** [File:Array of breads.jpg](https://commons.wikimedia.org/wiki/File:Array_of_breads.jpg)
- **Author** PMATAS
- **Licence** CC BY-SA 4.0

## Lighthouse film tile

- **File** `res/drawable-*/art_film_lighthouse.png`
- **Source** [File:Sydney (AU), Macquarie Lighthouse -- 2019 -- 3437.jpg](https://commons.wikimedia.org/wiki/File:Sydney_(AU),_Macquarie_Lighthouse_--_2019_--_3437.jpg)
- **Author** Dietmar Rabich
- **Licence** CC BY-SA 4.0

## Western film tile

- **File** `res/drawable-*/art_film_western.png`
- **Source** [File:Surreal Sunrise in Oljato-Monument Valley (Unsplash).jpg](https://commons.wikimedia.org/wiki/File:Surreal_Sunrise_in_Oljato-Monument_Valley_(Unsplash).jpg)
- **Author** Andrew Coelho
- **Licence** CC0

## Wedding film tile

- **File** `res/drawable-*/art_film_wedding.png`
- **Source** [File:Wedding aisle decorated1.jpg](https://commons.wikimedia.org/wiki/File:Wedding_aisle_decorated1.jpg)
- **Author** Jina Lee
- **Licence** CC BY-SA 3.0

## "The Walk" film tile

- **File** `res/drawable-*/art_film_walk.png`
- **Source** [File:Footpath from a country lane - geograph.org.uk - 5763524.jpg](https://commons.wikimedia.org/wiki/File:Footpath_from_a_country_lane_-_geograph.org.uk_-_5763524.jpg)
- **Author** Andrew Hill
- **Licence** CC BY-SA 2.0

## Hospital box set tile

- **File** `res/drawable-*/art_set_hospital.png`
- **Source** [File:Red Cross Society Central Hospital corridor 2014 Museum Meiji Mura.jpg](https://commons.wikimedia.org/wiki/File:Red_Cross_Society_Central_Hospital_corridor_2014_Museum_Meiji_Mura.jpg)
- **Author** Morio
- **Licence** CC BY-SA 4.0

## "Sisters" box set tile

- **File** `res/drawable-*/art_set_sisters.png`
- **Source** [File:Portrait of two women - DPLA - 3526cf6b794dd76cb801973e98fcf0ec.jpg](https://commons.wikimedia.org/wiki/File:Portrait_of_two_women_-_DPLA_-_3526cf6b794dd76cb801973e98fcf0ec.jpg)
- **Author** Unknown author
- **Licence** Public domain

## Allotment box set tile

- **File** `res/drawable-*/art_set_allotment.png`
- **Source** [File:-2018-10-09 Greenhouse and vegetable garden , Antingham.JPG](https://commons.wikimedia.org/wiki/File:-2018-10-09_Greenhouse_and_vegetable_garden_,_Antingham.JPG)
- **Author** Kolforn
- **Licence** CC BY-SA 4.0

## Family videos: school concert tile

- **File** `res/drawable-*/art_fam_concert.png`
- **Source** [File:Elementary School Choir.jpg](https://commons.wikimedia.org/wiki/File:Elementary_School_Choir.jpg)
- **Author** Stilfehler
- **Licence** CC BY-SA 3.0

## Family videos: dog tile

- **File** `res/drawable-*/art_fam_dog.png`
- **Source** [File:West Highland White Terrier lying on lawn May 2023.jpg](https://commons.wikimedia.org/wiki/File:West_Highland_White_Terrier_lying_on_lawn_May_2023.jpg)
- **Author** Dominic Nelson
- **Licence** CC BY-SA 4.0

## Family videos: birthday tile

- **File** `res/drawable-*/art_fam_birthday.png`
- **Source** [File:Birthday cake and presents, Downpatrick, April 2010.JPG](https://commons.wikimedia.org/wiki/File:Birthday_cake_and_presents,_Downpatrick,_April_2010.JPG)
- **Author** Ardfern
- **Licence** CC BY-SA 3.0
