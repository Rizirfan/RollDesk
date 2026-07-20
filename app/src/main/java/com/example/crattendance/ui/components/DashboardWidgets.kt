package com.example.crattendance.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .padding(0.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun QuickActionChip(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun SleekCircularChart(
    percentage: Float,
    label: String,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp = 100.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = percentage / 100f,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = color.copy(alpha = 0.1f),
                    style = Stroke(width = strokeWidth.toPx())
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${percentage.toInt()}%",
                    fontSize = (size.value * 0.18f).sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SleekBarChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val maxVal = data.maxOf { it.second }.coerceAtLeast(1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .height(160.dp)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { item ->
                val barHeightRatio = item.second / maxVal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.fillMaxHeight().weight(1f)
                ) {
                    Text(
                        text = "${item.second.toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = barColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxHeight(barHeightRatio.coerceAtLeast(0.05f))
                            .width(16.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(barColor.copy(alpha = 0.85f))
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { item ->
                Text(
                    text = item.first,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
