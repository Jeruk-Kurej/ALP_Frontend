package com.jeruk.alp_frontend.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.nio.file.WatchEvent
import com.jeruk.alp_frontend.R

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun home(){
Column (horizontalAlignment = Alignment.CenterHorizontally){
    Text("Sum-O", color = Color.Magenta, fontSize = 20.sp, textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
    Text("Sum it up. Own your business", textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
    Box(
        modifier = Modifier.padding( 16.dp).size(200.dp).border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center,) {
        Text("Sistem POS dan manajemen bisnis yang modern, mudah, dan powerful untuk membantu Anda mengelola bisnis dengan lebih baik.")
    }
    Row {
        Button(onClick = {}, Modifier.background(color = Color.White), shape = RoundedCornerShape(20.dp)) {
            Text("Lewati", color = Color.Black)
        }
        Button(onClick = {}, Modifier.background(color = Color.Magenta), shape = RoundedCornerShape(20.dp)) {
            Text("selanjutnya", color = Color.White)
            Image(painter = painterResource(id = R.drawable.baseline_arrow_forward_24), contentDescription = "arrow")
        }
    }
}
}
