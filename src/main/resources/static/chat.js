document.addEventListener("DOMContentLoaded", () => {
    const chatWidget  = document.getElementById("chatWidget");
    const chatToggle  = document.getElementById("chatToggle");
    const closeChat   = document.getElementById("closeChat");
    const chatMessages = document.getElementById("chatMessages");
    const chatInput   = document.getElementById("chatInput");
    const sendBtn     = document.getElementById("sendChat");

    if (!chatWidget || !chatToggle) return;

    // Open / close
    chatToggle.addEventListener("click", () => {
        chatWidget.classList.toggle("hidden");
        if (!chatWidget.classList.contains("hidden")) chatInput.focus();
    });
    closeChat.addEventListener("click", () => chatWidget.classList.add("hidden"));

    // Send on button or Enter
    sendBtn.addEventListener("click", sendMessage);
    chatInput.addEventListener("keypress", e => { if (e.key === "Enter") sendMessage(); });

    async function sendMessage() {
        const text = chatInput.value.trim();
        if (!text) return;
        appendBubble(text, "user");
        chatInput.value = "";

        // Typing indicator
        const typingId = "typing-" + Date.now();
        appendTyping(typingId);

        try {
            const res  = await fetch("/api/chat", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ message: text })
            });
            const data = await res.json();
            document.getElementById(typingId)?.remove();
            appendBubble(data.response, "bot");
        } catch {
            document.getElementById(typingId)?.remove();
            appendBubble("⚠️ Neural link interrupted. Please try again.", "bot");
        }
    }

    function appendBubble(text, role) {
        const wrap = document.createElement("div");
        wrap.className = `chat-bubble ${role}`;

        const now = new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
        const formatted = text.replace(/\n/g, "<br>");

        if (role === "bot") {
            wrap.innerHTML = `
                <div class="bubble-avatar"><img src="/images/ai-avatar.png" alt="NET" style="width:100%;height:100%;border-radius:6px;object-fit:cover;"></div>
                <div class="bubble-content">
                    ${formatted}
                    <div class="bubble-time">${now}</div>
                </div>`;
        } else {
            wrap.innerHTML = `
                <div class="bubble-content">
                    ${formatted}
                    <div class="bubble-time">${now}</div>
                </div>`;
        }

        chatMessages.appendChild(wrap);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function appendTyping(id) {
        const wrap = document.createElement("div");
        wrap.className = "chat-bubble bot";
        wrap.id = id;
        wrap.innerHTML = `
            <div class="bubble-avatar"><img src="/images/ai-avatar.png" alt="NET" style="width:100%;height:100%;border-radius:6px;object-fit:cover;"></div>
            <div class="bubble-content">
                <div class="typing-indicator">
                    <span></span><span></span><span></span>
                </div>
            </div>`;
        chatMessages.appendChild(wrap);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
});

// Quick reply helper (called from inline onclick)
function sendQuick(text) {
    const input = document.getElementById("chatInput");
    if (input) {
        input.value = text;
        document.getElementById("sendChat").click();
        // Remove quick reply buttons after first use
        document.querySelector(".chat-quick-replies")?.remove();
    }
}
