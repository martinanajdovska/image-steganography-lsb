This project explores how the most common algorithm for image steganography Least Significant Bit works. There are two variations of the algorithm, one that encodes one letter of the message in one pixel, 
and the other encodes one letter of the message in three pixels. Both don't have any visible effect on the image noticable by the human eye.

The algorithms are tested with a simple web application. The message to be encoded and the image to be used are both entered in a form. The algorithms only work with images in PNG format.
After processing the user can download the encoded image to their local machine. The user can choose which algorithm they want to use by selecting one of the buttons. 
If the 1 in 1 algorithm is used, the message must end with '#' that's used as an end-of-message flag. The 1 in 3 algorithm
uses one of the color channels as a flag.

<img width="1449" height="472" alt="Screenshot 2025-12-01 002516" src="https://github.com/user-attachments/assets/1d0ea716-5cec-4c46-97ef-68112c8456ae" />
<img width="1442" height="285" alt="Screenshot 2025-12-01 003600" src="https://github.com/user-attachments/assets/e3f6d778-3cda-4edd-b5cf-050a59621767" />
<img width="1555" height="300" alt="Screenshot 2025-12-01 002503" src="https://github.com/user-attachments/assets/9ee68225-647d-4764-8377-b3c240c4b0f8" />
