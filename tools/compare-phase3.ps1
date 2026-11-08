$ErrorActionPreference='Stop'
Add-Type -AssemblyName System.Drawing
$results=@()
foreach($theme in @('light','dark')) {
    $web=[System.Drawing.Bitmap]::new((Resolve-Path "docs/captures/reference-$theme.png").Path)
    $android=[System.Drawing.Bitmap]::new((Resolve-Path "docs/captures/phase3-$theme.png").Path)
    $sum=0.0; $count=0
    # Normalize the web's one-pixel border away. Exclude outside-circle pixels.
    for($y=0;$y -lt 298;$y++) { for($x=0;$x -lt 298;$x++) {
        if([Math]::Pow($x-148.5,2)+[Math]::Pow($y-148.5,2) -gt [Math]::Pow(148.5,2)) { continue }
        $a=$web.GetPixel($x+1,$y+1)
        $b=$android.GetPixel([int][Math]::Floor(($x+.5)*$android.Width/298),[int][Math]::Floor(($y+.5)*$android.Height/298))
        $sum += [Math]::Abs([int]$a.R-$b.R)+[Math]::Abs([int]$a.G-$b.G)+[Math]::Abs([int]$a.B-$b.B)
        $count+=3
    } }
    $results+=@{theme=$theme;meanAbsoluteRgbError=$sum/$count;webInternalDiameter=298;androidPixels=$android.Width;
        note='Nearest-neighbor normalized global difference, includes typography and 3D; not a geometric pixel-perfect score.'}
    $web.Dispose(); $android.Dispose()
}
$results | ConvertTo-Json | Set-Content -Encoding utf8 docs/phase3-comparison.json
$results | Format-Table
