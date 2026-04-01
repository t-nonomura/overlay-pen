Add-Type -AssemblyName System.Drawing

$outputDir = "D:\overlay-pen\store-assets\google-play"
$outputPath = Join-Path $outputDir "icon-512.png"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$size = 512
$bitmap = New-Object System.Drawing.Bitmap $size, $size
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)

$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$graphics.Clear([System.Drawing.Color]::Transparent)

function New-RoundedRectPath {
    param(
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height,
        [float]$Radius
    )

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $Radius * 2

    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()

    return $path
}

$backgroundRect = New-Object System.Drawing.RectangleF(24, 24, 464, 464)
$backgroundPath = New-RoundedRectPath -X 24 -Y 24 -Width 464 -Height 464 -Radius 108
$gradientBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    [System.Drawing.PointF]::new(48, 48),
    [System.Drawing.PointF]::new(448, 448),
    [System.Drawing.Color]::FromArgb(0x13, 0x22, 0x38),
    [System.Drawing.Color]::FromArgb(0x0B, 0x72, 0x85)
)
$graphics.FillPath($gradientBrush, $backgroundPath)

$overlayBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(52, 255, 255, 255))
$graphics.FillEllipse($overlayBrush, 330, 36, 120, 120)

$phonePath = New-RoundedRectPath -X 126 -Y 94 -Width 260 -Height 324 -Radius 38
$phonePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(248, 250, 252), 20)
$phonePen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
$graphics.DrawPath($phonePen, $phonePath)

$speakerPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(217, 226, 236), 12)
$speakerPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$speakerPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($speakerPen, 220, 134, 292, 134)

$strokePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(245, 158, 11), 28)
$strokePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$strokePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$strokePen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
$graphics.DrawBezier($strokePen, 170, 340, 206, 302, 258, 242, 332, 198)

$highlightPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(253, 230, 138), 10)
$highlightPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$highlightPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($highlightPen, 160, 350, 194, 316)

$dotBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(11, 114, 133))
$graphics.FillEllipse($dotBrush, 142, 340, 36, 36)

$bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$dotBrush.Dispose()
$highlightPen.Dispose()
$strokePen.Dispose()
$speakerPen.Dispose()
$phonePen.Dispose()
$overlayBrush.Dispose()
$gradientBrush.Dispose()
$backgroundPath.Dispose()
$phonePath.Dispose()
$graphics.Dispose()
$bitmap.Dispose()

Write-Output "Generated $outputPath"
