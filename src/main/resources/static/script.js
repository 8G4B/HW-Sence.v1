document.addEventListener("DOMContentLoaded", function() {
    const cpuGauge = document.getElementById('cpuGauge');
    if(cpuGauge) {
        const cpuContext = cpuGauge.getContext('2d');
        const cpuUsed = parseFloat(cpuGauge.getAttribute('data-cpu-used')) || 0.0;

        new Chart(cpuContext, {
            type:'doughnut',
            data:{
                labels:['Used','Free'],
                datasets:[{
                    data:[cpuUsed,100-cpuUsed],
                    backgroundColor:['rgba(255,99,132,0.7)','rgba(201,203,207,0.3)'],
                    borderColor:['#fff','#fff'],
                    borderWidth:1
                }]
            },
            options:{
                cutout:'70%',
                plugins:{
                    legend:{display:false},
                    tooltip:{
                        callbacks:{
                            label:function(ctx){return ctx.label+': '+ctx.parsed+'%';}
                        }
                    }
                }
            }
        });
    }

    const memChart = document.getElementById('memChart');
    if(memChart) {
        const memCtx = memChart.getContext('2d');
        const labels = JSON.parse(memChart.getAttribute('data-process-names') || '[]');
        const values = JSON.parse(memChart.getAttribute('data-process-values') || '[]');

        new Chart(memCtx, {
            type:'bar',
            data:{
                labels: labels,
                datasets:[{
                    label:'Memory Usage (MB)',
                    data: values,
                    backgroundColor:'rgba(75,192,192,0.2)',
                    borderColor:'rgba(75,192,192,1)',
                    borderWidth:1
                }]
            },
            options:{
                scales:{y:{beginAtZero:true}}
            }
        });
    }
});