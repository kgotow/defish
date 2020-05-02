# defish
defish photo images taken by fish-eye lens.

before:
<img src="https://github.com/kgotow/defish/blob/master/samples/K1032025.jpg" width="400"/>

after:
<img src="https://github.com/kgotow/defish/blob/master/samples/K1032025-df-0.65-16_9.jpg" width="400"/>

### Usage:
java -jar defish.jar [-a=AMOUNT] [-c=WIDTH:HEIGHT] [-q=QUALITY] &lt;INPUT FILES or DIRECTORIES&gt;

### [-a=AMOUNT]
amount of defish correction

0.0 <= AMOUNT <= 1.0

default: 0.65

recommended: 0.6 <= AMMOUNT <= 0.8, depend on field of view (0.65 for 17mm, 0.7 for 16mm)


  
### [-c=WIDTH:HEIGHT]
crop aspect ratio

WIDTH:HEIGHT = 16:9, 3:2, 4:3, 1:1 or any ratio

default: aspect ratio of each original images

recommended: 19:6 for landscapes
    
### [-q=QUALITY]
jpeg quality
0.0 <= QUALITY <= 1.0
default: 0.8
    
### &lt;INPUT FILES or DIRECTORIES ...&gt;
jpeg files or directories
  
### &lt;OUTPUT FILES&gt;
jpeg files created in same directories of input files with suffix "-df-AMOUNT-WIDHT_HEIGHT"

example: image.jpg -> image-df-0.65-16_9.jpg
  
### Examples:
java -jar defish.jar -a=0.65 -c=16:9 image1.jpg image2.jpg

java -jar defish.jar -a=0.7 -q=0.9 dir1 dir2 file1.jpg file2.jpg
