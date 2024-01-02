/* Must use an interpolated color scale, which has a range of [0, 1] */
export function interpolateColors(dataLength, colorRangeInfo) {
    let { colorStart, colorEnd } = colorRangeInfo
    let colorRange = colorEnd - colorStart
    let intervalSize = colorRange / dataLength
    let i, colorPoint
    let colorArray = []

    for (i = 0; i < dataLength; i++) {
        colorPoint = calculatePoint(i, intervalSize, colorRangeInfo)
        //colorArray.push(colorPoint)
        colorArray.push('#'+colorPoint.toString(16))
    }

    return colorArray
}

function calculatePoint(i, intervalSize, colorRangeInfo) {
    let { colorStart, colorEnd, useEndAsStart } = colorRangeInfo
    return (useEndAsStart
        ? (colorEnd - (i * intervalSize))
        : (colorStart + (i * intervalSize)))
}

export const colorRangeRainbow = {
    colorStart: 0,
    colorEnd: 1,
    useEndAsStart: true,
}

export const colorRangeCool = {
    colorStart: 0,
    colorEnd: 0.65,
    useEndAsStart: true,
}