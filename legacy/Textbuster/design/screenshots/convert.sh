#!/bin/sh

for file in `ls *| grep -v "\."`
do
 convert -depth 8 -size 480x800 RGBA:$file $file.png
 mv $file-0.png $file.png
 rm $file-*
done
