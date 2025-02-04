const timeCtx = document.querySelector('.now-time');
function updateTime() {
    const now = new Date();
    const dateNow = now.toISOString().split('T')[0];
    const timeNow = now.toTimeString().split(' ')[0];
    timeCtx.textContent = dateNow + ' ' + timeNow;
}
setInterval(updateTime, 1000);

const cpuCtx = document.getElementById('cpuGauge');
if (cpuCtx) {
    const cpuUsed = parseFloat(cpuCtx.getAttribute('data-cpu-used')) || 0.0;
    new Chart(cpuCtx.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: ['Used', 'Free'],
            datasets: [{
                data: [cpuUsed, 100 - cpuUsed],
                backgroundColor: ['rgba(255,99,132,0.7)', 'rgba(201,203,207,0.3)'],
                borderColor: ['#fff', '#fff'],
                borderWidth: 1
            }]
        },
        options: {
            cutout: '70%',
            plugins: {
                legend: { display: false },
                title: {
                    display: true,
                    text: 'CPU',
                    color: '#000',
                    position: 'bottom',
                    font: { size: 16, weight: 'bold' }
                },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return ctx.label + ': ' + ctx.parsed + '%';
                        }
                    }
                }
            }
        }
    });
}

const memG = document.getElementById('memGauge');
if (memG) {
    const memUsed = parseFloat(memG.getAttribute('data-mem-used')) || 0.0;
    new Chart(memG.getContext('2d'), {
        type: 'doughnut',
        data: {
            labels: ['Used', 'Free'],
            datasets: [{
                data: [memUsed, 100 - memUsed],
                backgroundColor: ['rgba(54,162,235,0.7)', 'rgba(201,203,207,0.3)'],
                borderColor: ['#fff', '#fff'],
                borderWidth: 1
            }]
        },
        options: {
            cutout: '70%',
            plugins: {
                legend: { display: false },
                title: {
                    display: true,
                    text: 'Memory',
                    color: '#000',
                    position: 'bottom',
                    font: { size: 16, weight: 'bold' }
                },
                tooltip: {
                    callbacks: {
                        label: function(ctx) {
                            return ctx.label + ': ' + ctx.parsed + '%';
                        }
                    }
                }
            }
        }
    });

    const memTotal = memG.getAttribute('data-mem-total') || "N/A";
    const memUsedPhysical = memG.getAttribute('data-mem-used-physical') || "N/A";
    const memDetailsElem = document.getElementById('memUsageDetails');
    if (memDetailsElem) {
        memDetailsElem.textContent = `Used: ${memUsedPhysical} / Total: ${memTotal}`;
    }
}

const memC = document.getElementById('memChart');
if (memC) {
    new Chart(memC.getContext('2d'), {
        type: 'bar',
        data: {
            labels: memC.getAttribute('data-process-names'),
            datasets: [{
                label: 'Memory Usage (MB)',
                data: memC.getAttribute('data-process-values'),
                backgroundColor: 'rgba(75,192,192,0.2)',
                borderColor: 'rgba(75,192,192,1)',
                borderWidth: 1
            }]
        },
        options: {
            scales: { y: { beginAtZero: true } }
        }
    });
}