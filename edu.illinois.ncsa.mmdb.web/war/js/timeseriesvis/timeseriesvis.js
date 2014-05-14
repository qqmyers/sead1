/*! timeseriesvis 2014-05-13 19:05:21 */
function update() {
    var a = $("#xCol option:selected").val(), b = sortDataByKey(originalData, a), c = projectDataByKey(b, a);
    console.log(c), render(c);
}

function setupControls() {
    d3.select("#graphControls").append("text").text("x-axis"), d3.select("#graphControls").append("select").attr("id", "xCol").on("change", update);
}

function projectData(a) {
    for (var b = [], c = Object.keys(a[0]), d = c.length, e = 0; d - 1 > e; e++) b[e] = {
        key: c[e + 1],
        values: []
    };
    for (var f = 0; f < a.length; f++) for (var e = 1; d > e; e++) {
        var c = Object.keys(a[f]), g = {};
        g.x = a[f][c[0]], g.y = a[f][c[e]], b[e - 1].values.push(g);
    }
    return b;
}

function projectDataByKey(a, b) {
    for (var c = [], d = Object.keys(a[0]), e = d.length, f = 0; e > f; f++) b !== d[f] && c.push({
        key: d[f],
        values: []
    });
    for (var g = 0; g < a.length; g++) for (var d = Object.keys(a[g]), f = 0; e > f; f++) if (d[f] !== b) {
        var h = {};
        h.x = a[g][b], h.y = a[g][d[f]], $.each(c, function(a, b) {
            b.key === d[f] && b.values.push(h);
        });
    }
    return c;
}

function sortDataByKey(a, b) {
    return console.log("sorting by " + b), a.sort(function(a, c) {
        return a[b] - c[b];
    }), a;
}

function updateControls(a) {
    $("#xCol option").remove(), $("#yCheckboxes input").remove(), $("label[for=yRadio]").remove();
    $.each(Object.keys(a[0]), function() {
        $("#xCol").append($("<option />").val(this).text(this));
    }), defaultX = Object.keys(a[0])[0], console.log("controls updated");
}

function isValidDate(a) {
    return "[object Date]" === Object.prototype.toString.call(a) ? isNaN(a.getTime()) ? !1 : !0 : !1;
}

function loadData() {
    var a = $("#csvFile").val();
    loadDataByUrl(a);
}

function loadDataByUrl(a) {
    console.log("Loading data at " + a), d3.csv(a, function(a, b) {
        updateControls(b), b.forEach(function(a) {
            Object.keys(a).forEach(function(b) {
                b === defaultX ? moment(a[b]).isValid() && (console.log("Is valid date: " + a[b]), 
                a[b] = moment(a[b])) : a[b] = 1 * a[b];
            });
        }), originalData = b, b = sortDataByKey(b, defaultX);
        var c = projectData(b);
        render(c);
    });
}

function render(a) {
    console.log("rendering data"), console.log(a), nv.addGraph(function() {
        var b = a[0].values[0].x;
        return (isNaN(b) || moment(new Date(b.toString()), dateFormats).isValid()) && (console.log("setting x axis to date format"), 
        chart.xAxis.tickFormat(function(a) {
            return d3.time.format("%Y-%m-%d")(new Date(a));
        }), chart.x2Axis.tickFormat(function(a) {
            return d3.time.format("%Y-%m-%d")(new Date(a));
        })), chart.yAxis, chart.y2Axis, d3.select("#d3TimeseriesVis svg").datum(a).transition().duration(500).call(chart), 
        nv.utils.windowResize(chart.update), chart;
    });
}

var originalData, chart = nv.models.lineWithFocusChart(), dateFormats = [ "MM-DD-YYYY", "DD-MM", "DD-MM-YYYY", "YYYY-MM-DD" ];