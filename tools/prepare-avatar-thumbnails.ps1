$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
Set-Location (Split-Path -Parent $PSScriptRoot)
# Crop the existing real GPU renders; keep a small margin and preserve transparency.
foreach ($modelId in @('holo','fox','pajaro','mapache','moneda','riot')) {
    $source=[System.Drawing.Bitmap]::new((Join-Path $PWD "docs/phase5-gpu/$modelId.png"))
    try {
        $left=$source.Width; $top=$source.Height; $right=0; $bottom=0
        for($pixelY=0; $pixelY -lt $source.Height; $pixelY++) {
            for($pixelX=0; $pixelX -lt $source.Width; $pixelX++) {
                if($source.GetPixel($pixelX,$pixelY).A -gt 64) {
                    $left=[Math]::Min($left,$pixelX); $top=[Math]::Min($top,$pixelY)
                    $right=[Math]::Max($right,$pixelX); $bottom=[Math]::Max($bottom,$pixelY)
                }
            }
        }
        if($right -lt $left -or $bottom -lt $top) { throw "Empty thumbnail: $modelId" }
        $margin=[int]([Math]::Max($right-$left+1,$bottom-$top+1)*.06)
        $left=[Math]::Max(0,$left-$margin); $top=[Math]::Max(0,$top-$margin)
        $right=[Math]::Min($source.Width-1,$right+$margin); $bottom=[Math]::Min($source.Height-1,$bottom+$margin)
        $cropped=$source.Clone([System.Drawing.Rectangle]::new($left,$top,$right-$left+1,$bottom-$top+1),
            [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            $cropped.Save((Join-Path $PWD "app/src/main/res/drawable-nodpi/avatar_$modelId.png"),
                [System.Drawing.Imaging.ImageFormat]::Png)
        } finally { $cropped.Dispose() }
    } finally { $source.Dispose() }
}
