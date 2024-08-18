package com.example.fireapp.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fireapp.R

@Composable
fun Chatting(modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf(TextFieldValue("")) }


//Box (
//    modifier = Modifier
//        .fillMaxSize()
//        .padding(10.dp),
////    contentAlignment = Alignment.TopCenter
//){
    Column(
        modifier = Modifier.fillMaxSize(),Arrangement.spacedBy(370.dp,Alignment.Top)
    ) {

        Text(text = "Group Chat", modifier = Modifier
            .padding(8.dp)
            .padding(start = 90.dp), fontSize = 35.sp,)
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {

        }
//
//        LazyColumn(
//            modifier = Modifier
//                .padding(8.dp)
//                .size(100.dp)
//                .fillMaxWidth()
//
//        ) {
//            items(100) {
//                Card {
//
//                }
//            }
//
//
//        }
        Row {
            OutlinedIconButton(onClick = { /*TODO*/ },
                modifier = Modifier.size(40.dp).fillMaxWidth(),
            ) {
                Image(painterResource(id = R.drawable.pngegg),
                    modifier = Modifier.size(45.dp)
                    , contentDescription = null)

            }
            TextField(
                value = text,
                onValueChange = {
                    text = it
                },
//                label = { Text(text = "Your Label") },
                placeholder = { Text(text = "massages .....") },
                modifier = Modifier.padding(start = 5.dp,end = 8.dp),

            )

            OutlinedIconButton(onClick = { /*TODO*/ },
                modifier = Modifier.size(55.dp).fillMaxWidth(),
                ) {
                Image(painterResource(id = R.drawable.send_icon),
                    modifier = Modifier.size(45.dp)
                    , contentDescription = null)

            }
        }
        }
    }

//}


@Preview
@Composable
private fun ChattingPreview() {
    Chatting()
}