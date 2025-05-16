class Checkers {
    constructor(canvas, endpoint) {
        this.canvas = canvas;
        this.ctx = canvas.getContext("2d");
        this.size = 8; 
        this.cellSize = this.canvas.width / this.size;
        this.board = Array(this.size * this.size).fill(0); 
        this.endpoint = endpoint; 
        this.currentPlayer = 1; 
        this.map = new Map(); 
        this.array = []; 
        this.moves = []; 
        this.selectedPieceIndex = null;
       
        this.test = false;

        
        this.images = {
            board: new Image(),
            playerPiece: new Image(),
            playerKing: new Image(),
            opponentPiece: new Image(),
            opponentKing: new Image(),
        };

        
        this.images.board.src = "board1.png"; 
        this.images.playerPiece.src = "black_pawn.png"; 
        this.images.playerKing.src = "black_pawn_king.png"; 
        this.images.opponentPiece.src = "white_pawn.png"; 
        this.images.opponentKing.src = "white_pawn_king.png"; 

        
        this.images.board.onload = () => {
            this.initBoard();
            this.drawBoard();
        };

        
        this.canvas.addEventListener("click", (event) => {
            const indexY = Math.floor(event.offsetY / this.cellSize); 
            const indexX = Math.floor(event.offsetX / this.cellSize); 
            const index = indexX + indexY * this.size; 

            const piece = this.board[index];
            if (piece === 0) {
                this.secondClick(indexY, indexX, index);
            } else {
                this.firstClick(indexY, indexX, index);
            }
        });
    }

   
    initBoard() {
        for (let y = 0; y < this.size; y++) {
            for (let x = 0; x < this.size; x++) {
                const pos = x + y * this.size;
                if ((x + y) % 2 === 0) {
                    if (y < 3) {
                        this.board[pos] = -1; 
                    } else if (y > 4) {
                        this.board[pos] = 1; 
                    }
                }
            }
        }
    }

    
    drawPiece(piece, x, y) {
        const SIZE = this.cellSize;
        const margin = SIZE / 10;

        let image;
        if (piece === 1) image = this.images.playerPiece;
        else if (piece === 2) image = this.images.playerKing;
        else if (piece === -1) image = this.images.opponentPiece;
        else if (piece === -2) image = this.images.opponentKing;

        this.ctx.drawImage(
            image,
            x + margin,
            y + margin,
            SIZE - 2 * margin,
            SIZE - 2 * margin
        );
    }

    
    async drawBoard(calling = true  ,board = this.board, from = -1, to = -1, currentPlayer = this.currentPlayer) {
        this.currentPlayer = currentPlayer;
        const SIZE = this.cellSize;
        this.board = board;

        
        this.ctx.drawImage(this.images.board, 0, 0, this.canvas.width, this.canvas.height);

        
        if (from >= 0) this.highlightCell(from, "green");
        if (to >= 0) this.highlightCell(to, "green");

        
        for (let y = 0; y < this.size; y++) {
            for (let x = 0; x < this.size; x++) {
                const pos = x + y * this.size;
                const xPos = x * SIZE;
                const yPos = y * SIZE;

                const piece = board[pos];
                if (piece !== 0) {
                    this.drawPiece(piece, xPos, yPos);
                }
            }
        }

        if (calling) {
            let message = `${currentPlayer}`;
            this.call(message);
            await this.waitForMove();
        } else {
            this.mandatoryCapture(this.map);
       }
    }

    highlightCell(index, color) {
        if (index < 0) return;
        this.lightCell(index, color, false);
    }

    lightCell(index, color, preserveImage = true) {
        const SIZE = this.cellSize;
        const x = (index % this.size) * SIZE;
        const y = Math.floor(index / this.size) * SIZE;
        const piece = this.board[index];

        if (preserveImage && piece !== 0) {
            this.ctx.clearRect(x, y, SIZE, SIZE);
        }

        this.ctx.fillStyle = color;
        this.ctx.globalAlpha = 0.5;
        this.ctx.fillRect(x, y, SIZE, SIZE);
        this.ctx.globalAlpha = 1.0;

        if (preserveImage && piece !== 0) {
            this.drawPiece(piece, x, y);
        }
    }

 drawMessage(message) {
        this.ctx.font = "30px Arial"; 
        this.ctx.fillStyle = "blue";   
        this.ctx.textAlign = "center"; 
        this.ctx.fillText(message, this.canvas.width / 2, this.canvas.height/2);
        
    }

   
    Turn(index) {
        return this.board[index] != 0 && (this.board[index] * this.currentPlayer > 0);
    }

    
    call(message) {
        fetch(this.endpoint, {
            method: "POST",
            headers: { "Content-Type": "text/plain" },
            body: message.toString(),
        }).catch((error) => console.error("Fetch error:", error));
    }

    
    async handleNormalMove(from, to, index) {
        let message = `${from},${to}`;
        this.call(message);
        await this.waitForMove();

        if (this.moves.length > 0) {
            this.lightCell(index, "green");
            this.moves.forEach((element) => {
                const pos = element[2] * this.size + element[3];
                this.highlightCell(pos, "green");
            });
            this.array = [from, to, index];
        } else {
            this.lightCell(index, "red");
        }
    }

    
   async firstClick(from, to, index) {

        if (this.array[2] === index) return;
        if (!this.Turn(index)) {
            this.lightCell(index, "red");
            return;
        }

        this.drawBoard(false);
        if (this.map.size > 0) {
            if (this.map.has(index)) {
                const captures = this.map.get(index);
                this.drawBoard(false);
                captures.forEach((element) => {
                    let found = true;
                    for (let i = element.length - 1; i >= 0; i--) {
                         if (i === 0) {this.lightCell(element[i], "green"); return ; }
                        if (found) {
                            if (this.board[element[i]] === 0) {
                                this.highlightCell(element[i], "green");
                            }
                          if(this.board[element[i-1]] != 0 )  found = false;
                        } else {
                            this.lightCell(element[i], "blue");
                        }
                       
                    }
                });
                this.array = [from, to, index];
            } else {
                this.lightCell(index, "red");
            }
        } else {
            this.handleNormalMove(from, to, index);
        }
    }

    waitForMove() {
        return new Promise((resolve) => {
            this.resolveMove = resolve;
        });
    }

    manageMove(array) {
        this.moves = array;
        if (this.resolveMove) {
            this.resolveMove();
            this.resolveMove = null;
        }
    }

    secondClick(from, to, index) {
        if (this.array.length > 0) {
            if (this.map.size > 0) {
                if (this.map.has(this.array[2])) {
                    let found = false;
                    const capture = this.map.get(this.array[2]);
                    capture.forEach((element) => {
                        for ( let i = element.length -1 ; i >= 0 ; i--) {
                            if (this.board[element[i]] === 0) {
                            if (element[i] === index) found = true;
                        }  else break ; 
                        }
                        
                    });
                    if (found) {
                        let message = `${this.array[0]},${this.array[1]},${from},${to}`;
                        this.call(message);
                        this.map = new Map();
                    } else {
                        this.highlightCell(index, "red");
                    }
                } else {
                    this.highlightCell(index, "red");
                }
            } else if (this.moves.length > 0) {
                let found = false;
                this.moves.forEach((element) => {
                    if (element[2] === from && element[3] === to) found = true;
                });
                if (found) {
                    let message = `${this.array[0]},${this.array[1]},${from},${to}`;
                    this.call(message);
                    this.moves = [];
                } else {
                    this.highlightCell(index, "red");
                }
            }
        } else {
            this.highlightCell(index, "red");
        }
    }

    manageCapture(map, turn) {
        this.map = new Map(map);
      
        this.mandatoryCapture(this.map);
        if (this.resolveMove) {
            this.resolveMove();
            this.resolveMove = null;
        }
    }

    mandatoryCapture(map) {
        if (map.size > 0) {
            for (const key of map.keys()) {
                this.lightCell(key, "yellow");
            }
        }
    }

}




