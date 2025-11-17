async function loadSlots() {
    const cacheBuster = Date.now();
    try {
        const response = await fetch(`data/slots.json?cb=${cacheBuster}`);
        if (!response.ok) {
            throw new Error('Snapshot not found yet. Run the Java app once to generate it.');
        }
        const slots = await response.json();
        renderSlots(slots);
    } catch (err) {
        const grid = document.getElementById('slotGrid');
        grid.innerHTML = `<div class="slot-card occupied"><h3>No data</h3><p>${err.message}</p></div>`;
        document.getElementById('totalSlots').textContent = 'Total: 0';
        document.getElementById('freeSlots').textContent = 'Free: 0';
        document.getElementById('occupiedSlots').textContent = 'Occupied: 0';
    }
}

function renderSlots(slots) {
    const grid = document.getElementById('slotGrid');
    grid.innerHTML = '';
    let free = 0;
    let occupied = 0;

    slots.forEach(slot => {
        const card = document.createElement('article');
        card.className = `slot-card ${slot.occupied ? 'occupied' : 'free'}`;

        card.innerHTML = `
            <div class="badge">${slot.occupied ? 'Occupied' : 'Free'}</div>
            <h3>${slot.slotId}</h3>
            <p class="meta">Floor ${slot.floor} • ${slot.distance} m from gate</p>
            <p class="meta">${slot.occupied ? 'Vehicle: ' + slot.vehicle : 'Ready for allocation'}</p>
        `;

        grid.appendChild(card);
        if (slot.occupied) {
            occupied++;
        } else {
            free++;
        }
    });

    document.getElementById('totalSlots').textContent = `Total: ${slots.length}`;
    document.getElementById('freeSlots').textContent = `Free: ${free}`;
    document.getElementById('occupiedSlots').textContent = `Occupied: ${occupied}`;
}

loadSlots();
setInterval(loadSlots, 5000);

