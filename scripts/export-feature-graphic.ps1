Add-Type -AssemblyName System.Drawing

$outputDir = "D:\overlay-pen\store-assets\google-play"
$outputPath = Join-Path $outputDir "feature-graphic-1024x500.png"

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$width = 1024
$height = 500
$bitmap = New-Object System.Drawing.Bitmap $width, $height
$graphics = [System.Drawing.Graphics]::FromImage($bitmap)

$graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
$graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

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

function DrawTextBlock {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Text,
        [System.Drawing.Font]$Font,
        [System.Drawing.Brush]$Brush,
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height
    )

    $format = New-Object System.Drawing.StringFormat
    $format.Alignment = [System.Drawing.StringAlignment]::Near
    $format.LineAlignment = [System.Drawing.StringAlignment]::Near
    $Graphics.DrawString($Text, $Font, $Brush, [System.Drawing.RectangleF]::new($X, $Y, $Width, $Height), $format)
    $format.Dispose()
}

$graphics.Clear([System.Drawing.Color]::FromArgb(246, 241, 232))

$backgroundBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    [System.Drawing.PointF]::new(0, 0),
    [System.Drawing.PointF]::new($width, $height),
    [System.Drawing.Color]::FromArgb(19, 34, 56),
    [System.Drawing.Color]::FromArgb(11, 114, 133)
)
$graphics.FillRectangle($backgroundBrush, 0, 0, $width, $height)

$ambientBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(34, 245, 158, 11))
$graphics.FillEllipse($ambientBrush, 40, -30, 260, 260)
$graphics.FillEllipse($ambientBrush, 820, 300, 180, 180)

$panelPath = New-RoundedRectPath -X 40 -Y 36 -Width 944 -Height 428 -Radius 34
$panelBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(28, 255, 255, 255))
$panelBorder = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(56, 255, 255, 255), 2)
$graphics.FillPath($panelBrush, $panelPath)
$graphics.DrawPath($panelBorder, $panelPath)

$labelPath = New-RoundedRectPath -X 84 -Y 88 -Width 182 -Height 40 -Radius 20
$labelBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(34, 245, 158, 11))
$graphics.FillPath($labelBrush, $labelPath)

$labelFont = New-Object System.Drawing.Font("Segoe UI Semibold", 14, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$titleFont = New-Object System.Drawing.Font("Segoe UI Semibold", 42, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)
$subtitleFont = New-Object System.Drawing.Font("Segoe UI", 18, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$bulletFont = New-Object System.Drawing.Font("Segoe UI", 17, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
$chipFont = New-Object System.Drawing.Font("Segoe UI Semibold", 15, [System.Drawing.FontStyle]::Bold, [System.Drawing.GraphicsUnit]::Pixel)

$whiteBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(248, 250, 252))
$mutedBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(217, 226, 236))
$amberTextBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(253, 230, 138))
$tealBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(11, 114, 133))

DrawTextBlock -Graphics $graphics -Text "SCREEN DRAWING" -Font $labelFont -Brush $amberTextBrush -X 102 -Y 100 -Width 150 -Height 22
DrawTextBlock -Graphics $graphics -Text "Saizenmen Pen" -Font $titleFont -Brush $whiteBrush -X 84 -Y 144 -Width 380 -Height 60
DrawTextBlock -Graphics $graphics -Text "Draw annotations right on the screen." -Font $subtitleFont -Brush $mutedBrush -X 86 -Y 214 -Width 360 -Height 28

$bullets = @(
    "Draw over any app",
    "Quick color, width, and opacity controls",
    "Hide the tools when you need the app below"
)
$bulletY = 282
foreach ($bullet in $bullets) {
    $graphics.FillEllipse($tealBrush, 88, $bulletY + 7, 10, 10)
    DrawTextBlock -Graphics $graphics -Text $bullet -Font $bulletFont -Brush $whiteBrush -X 110 -Y $bulletY -Width 360 -Height 26
    $bulletY += 44
}

$shadowPath = New-RoundedRectPath -X 604 -Y 82 -Width 272 -Height 344 -Radius 42
$shadowBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(52, 12, 21, 33))
$graphics.FillPath($shadowBrush, $shadowPath)

$phonePath = New-RoundedRectPath -X 584 -Y 60 -Width 272 -Height 344 -Radius 42
$phoneBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(248, 250, 252))
$phoneFramePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(229, 236, 242), 3)
$graphics.FillPath($phoneBrush, $phonePath)
$graphics.DrawPath($phoneFramePen, $phonePath)

$screenPath = New-RoundedRectPath -X 606 -Y 98 -Width 228 -Height 268 -Radius 28
$screenBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
    [System.Drawing.PointF]::new(606, 98),
    [System.Drawing.PointF]::new(834, 366),
    [System.Drawing.Color]::FromArgb(252, 247, 239),
    [System.Drawing.Color]::FromArgb(232, 239, 244)
)
$graphics.FillPath($screenBrush, $screenPath)

$speakerPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(180, 198, 211), 6)
$speakerPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$speakerPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($speakerPen, 686, 79, 754, 79)

$annotationPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(245, 158, 11), 14)
$annotationPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$annotationPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$annotationPen.LineJoin = [System.Drawing.Drawing2D.LineJoin]::Round
$graphics.DrawBezier($annotationPen, 644, 286, 684, 244, 726, 202, 790, 170)

$circlePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(11, 114, 133), 10)
$graphics.DrawEllipse($circlePen, 694, 216, 86, 86)

$underlinePen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(37, 99, 235), 10)
$underlinePen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$underlinePen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($underlinePen, 650, 324, 762, 324)

$chipPath = New-RoundedRectPath -X 718 -Y 306 -Width 116 -Height 42 -Radius 18
$chipBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(224, 16, 42, 67))
$chipBorder = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(72, 11, 114, 133), 2)
$graphics.FillPath($chipBrush, $chipPath)
$graphics.DrawPath($chipBorder, $chipPath)

$chipIconPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(248, 250, 252), 5)
$chipIconPen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
$chipIconPen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
$graphics.DrawLine($chipIconPen, 738, 330, 750, 318)
$graphics.DrawLine($chipIconPen, 746, 316, 754, 324)
DrawTextBlock -Graphics $graphics -Text "Draw" -Font $chipFont -Brush $whiteBrush -X 760 -Y 317 -Width 48 -Height 18

$bitmap.Save($outputPath, [System.Drawing.Imaging.ImageFormat]::Png)

$chipIconPen.Dispose()
$chipBorder.Dispose()
$chipBrush.Dispose()
$underlinePen.Dispose()
$circlePen.Dispose()
$annotationPen.Dispose()
$speakerPen.Dispose()
$screenBrush.Dispose()
$screenPath.Dispose()
$phoneFramePen.Dispose()
$phoneBrush.Dispose()
$phonePath.Dispose()
$shadowBrush.Dispose()
$shadowPath.Dispose()
$tealBrush.Dispose()
$amberTextBrush.Dispose()
$mutedBrush.Dispose()
$whiteBrush.Dispose()
$chipFont.Dispose()
$bulletFont.Dispose()
$subtitleFont.Dispose()
$titleFont.Dispose()
$labelFont.Dispose()
$labelBrush.Dispose()
$panelBorder.Dispose()
$panelBrush.Dispose()
$panelPath.Dispose()
$ambientBrush.Dispose()
$backgroundBrush.Dispose()
$graphics.Dispose()
$bitmap.Dispose()

Write-Output "Generated $outputPath"
