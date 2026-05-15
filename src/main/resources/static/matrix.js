const canvas = document.getElementById('matrix-canvas');
const ctx = canvas.getContext('2d');

canvas.width = window.innerWidth;
canvas.height = window.innerHeight;

const katakana = 'アァカサタナハマヤャラワガザダバパイィキシチニヒミリヰギジヂビピウゥクスツヌフムユュルグズブヅプエェケセテネヘメレゲゼデベペオォコソトノホモヨョロゴゾドボポヴッン';
const latin = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
const nums = '0123456789';

const alphabet = katakana + latin + nums;

const fontSize = 14;
let columns = Math.floor(canvas.width / fontSize);

// Store each column's drop position and its fading characters
const drops = [];
for (let x = 0; x < columns; x++) {
    drops[x] = Math.floor(Math.random() * -50);
}

// Store fade values per cell
const grid = [];
for (let x = 0; x < columns; x++) {
    grid[x] = [];
    for (let y = 0; y < Math.ceil(canvas.height / fontSize) + 1; y++) {
        grid[x][y] = { char: '', alpha: 0 };
    }
}

const draw = () => {
    // Fully clear canvas each frame — page background shows through
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const rows = Math.ceil(canvas.height / fontSize) + 1;

    // Fade all existing characters
    for (let x = 0; x < columns; x++) {
        for (let y = 0; y < rows; y++) {
            if (grid[x] && grid[x][y] && grid[x][y].alpha > 0) {
                grid[x][y].alpha -= 0.03;
                if (grid[x][y].alpha < 0) grid[x][y].alpha = 0;
            }
        }
    }

    // Draw all faded characters
    ctx.font = fontSize + 'px monospace';
    ctx.shadowBlur = 0;
    ctx.shadowColor = 'transparent';

    for (let x = 0; x < columns; x++) {
        for (let y = 0; y < rows; y++) {
            if (grid[x] && grid[x][y] && grid[x][y].alpha > 0) {
                ctx.globalAlpha = grid[x][y].alpha;
                ctx.fillStyle = '#0ea5e9';
                ctx.fillText(grid[x][y].char, x * fontSize, y * fontSize);
            }
        }
    }

    // Place new leading characters
    for (let x = 0; x < columns; x++) {
        const y = drops[x];
        if (y >= 0 && y < rows) {
            if (grid[x] && grid[x][y]) {
                grid[x][y].char = alphabet.charAt(Math.floor(Math.random() * alphabet.length));
                grid[x][y].alpha = 0.9;
            }
        }
        drops[x]++;
        if (drops[x] * fontSize > canvas.height && Math.random() > 0.975) {
            drops[x] = Math.floor(Math.random() * -30);
        }
    }

    ctx.globalAlpha = 1.0;
};

setInterval(draw, 40);

window.addEventListener('resize', () => {
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;
    columns = Math.floor(canvas.width / fontSize);
});
