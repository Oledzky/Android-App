package com.example.pjatkapp

import android.R
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.example.pjatkapp.databinding.ProductInfoBinding
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat


class ProductInfo : DialogFragment() {

    companion object {
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }

    private lateinit var binding: ProductInfoBinding
    private lateinit var product: Product
    private lateinit var imageResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ProductInfoBinding.inflate(inflater, container, false)
        arguments?.let {
            val encodedImage = it.getString("productPhoto") ?: ""
            val photoByteArray = if (encodedImage.isNotEmpty()) {
                android.util.Base64.decode(encodedImage, android.util.Base64.DEFAULT)
            } else {
                byteArrayOf()
            }
            val bitmap = if (photoByteArray.isNotEmpty()) {
                BitmapFactory.decodeByteArray(photoByteArray, 0, photoByteArray.size)
            } else {
                null
            }
            binding.imageView.setImageBitmap(bitmap)
            val category = it.getString("productCategory") ?: ""
            val state = it.getString("productState") ?: ""
            val name = it.getString("productName") ?: ""
            val expirationDate = it.getString("productExpirationDate") ?: ""
            val quantity = it.getString("productQuantity") ?: ""
            val isDisposed = it.getBoolean("productIsDisposed") ?: false
            val productId = it.getInt("productId") ?: 0


            product = Product(
                category = category,
                state = state,
                photo = photoByteArray,
                name = name,
                expirationDate = expirationDate,
                quantity = quantity,
                isDisposed = isDisposed,
                id = productId
            )


            (binding.productNameField as TextView).text = product.name
            binding.dateButton.text = product.expirationDate
            (binding.quantityField as TextView).text = product.quantity
            binding.imageView.setImageBitmap(bitmap)
        } ?: run {
            product = Product(
                category = "",
                state = "",
                photo = byteArrayOf(),
                name = "",
                expirationDate = "",
                quantity = "",
                isDisposed = false,
                id = 0
            )
        }
        val categories = listOf("Kategoria", "Produkt spożywczy", "Lek", "Kosmetyk")
        val adapter = ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter
        categories.indexOf(product.category).let {
            val index = if (it == -1) 0 else it

            binding.categorySpinner.setSelection(index)

        }

        binding.dateButton.setOnClickListener {
            val datePicker = DatePickerDialog.OnDateSetListener() { _, year, month, dayOfMonth ->
                run {
                    val formatter = SimpleDateFormat("yyyy-MM-dd")
                    formatter.isLenient = false
                    val date = formatter.parse("$year-${month + 1}-$dayOfMonth")
                    binding.dateButton.text = formatter.format(date)

                }
            }
            DatePickerDialog(
                requireContext(), datePicker,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.imageView.setOnClickListener {
            println("Kliknięto na zdjęcie produktu.")
            pickImageFromGallery()
        }

        binding.saveButton.setOnClickListener {
            if (validateProduct()) {
                product.name = binding.productNameField.text.toString()
                product.expirationDate =
                    binding.dateButton.text.let { if (it == "....-..-..") "" else it.toString() }
                product.quantity = binding.quantityField.text.toString()
                product.isDisposed = false
                product.category = binding.categorySpinner.selectedItem as String
                if (product.id == 0) {
                    (activity as MainActivity).insertProduct(product)
                } else {
                    (activity as MainActivity).updateProduct(product)
                }
                dismiss()
            }
        }

        imageResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val imageUri = result.data?.data
                    val imageStream = requireContext().contentResolver.openInputStream(imageUri!!)
                    val selectedImage = BitmapFactory.decodeStream(imageStream)
                    val outputStream = ByteArrayOutputStream()
                    selectedImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    val imageBytes = outputStream.toByteArray()
                    val encodedImage =
                        android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)

                    val bundle = Bundle().apply {

                        putInt("productId", product.id)
                        putString("productName", product.name)
                        putString("productExpirationDate", product.expirationDate)
                        putString("productQuantity", product.quantity ?: "")
                        putBoolean("productIsDisposed", product.isDisposed)
                        putString("productCategory", product.category)
                        putString("productState", product.state)
                        putString("productPhoto", encodedImage)
                    }
                    ProductInfo().apply {
                        arguments = bundle
                    }.show(parentFragmentManager, "dialog")

                    binding.imageView.setImageBitmap(selectedImage)
                    product.photo = imageBytes
                }
            }

        return binding.root
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imageResultLauncher.launch(intent)
    }

    private fun validateProduct(): Boolean {

        val quantityPattern = Regex("""(\d+)([.,](\d+))?\s+(.+)""")

        if (binding.productNameField.text.isEmpty()) {
            Toast.makeText(context, "Nazwa produktu jest wymagana.", Toast.LENGTH_LONG).show()
            return false
        }

        if (binding.categorySpinner.selectedItemPosition == 0) {
            Toast.makeText(context, "Wybierz kategorię produktu.", Toast.LENGTH_LONG).show()
            return false
        }

        if (binding.dateButton.text.toString() == "....-..-..") {
            Toast.makeText(context, "Podaj datę ważności produktu.", Toast.LENGTH_LONG).show()
            return false
        }

        if (binding.dateButton.text.toString() < SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().time)) {
            Toast.makeText(
                context,
                "Data ważności produktu nie może być wcześniejsza niż dzisiejsza.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        if (binding.quantityField.text.isNotEmpty() && !quantityPattern.matches(binding.quantityField.text.toString())) {
            Toast.makeText(
                context,
                "Podaj ilość w formacie 'liczba jednostka', np. '100 ml', '2 szt.', '1 opakowanie'.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }
}