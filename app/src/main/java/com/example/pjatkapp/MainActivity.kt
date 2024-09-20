package com.example.pjatkapp

import ProductAdapter
import android.R
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Insert
import androidx.room.Room
import com.example.pjatkapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.Executors

//TODO: Ładnie gui zrobić
//TODO:
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: ProductAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DataBase
    private lateinit var products: List<Product>
    private val dbExecutor = Executors.newSingleThreadExecutor()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database =
            buildDataBase()

        populateDB()

        dbExecutor.execute {
            products = database.productDao().getAllProducts()

            runOnUiThread {
                updateUI()
            }

        }
    }

    private fun updateUI() {
        sortProducts()
        checkExpirationDate()
        setupRecyclerView()
        binding.itemCount.text = "Liczba produktów: ${products.size}"
        val productType = listOf("Wszystkie", "Produkt spożywczy", "Lek", "Kosmetyk")
        val expType = listOf("Wszystkie", "Przeterminowane", "Ważne")
        setupFilterControls(productType, expType)
        setupButton()
    }

    private fun setupButton() {
        binding.button.setOnClickListener(
            View.OnClickListener {
                ProductInfo().show(supportFragmentManager, "dialog")
            }
        )

    }

    private fun setupFilterControls(categories: List<String>, expType: List<String>){
        val prodSpinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, categories)
        prodSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val expSpinnerAdapter = ArrayAdapter(this, R.layout.simple_spinner_item, expType)
        expSpinnerAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val spinnerCategory = binding.typeSpinner
        val spinnerExp = binding.expSpinner

        spinnerExp.adapter = expSpinnerAdapter
        spinnerCategory.adapter = prodSpinnerAdapter
        spinnerCategory.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val cat =
                        if (spinnerCategory.selectedItemPosition == 0) null else spinnerCategory.selectedItem as String?
                    val filteredProducts = filterProducts(
                        products,
                        cat,
                        if(spinnerExp.selectedItemPosition == 0) null else spinnerExp.selectedItemPosition == 1
                    )
                    viewAdapter.notifyDataSetChanged()
                    recyclerView.adapter =
                        ProductAdapter(filteredProducts, supportFragmentManager, this@MainActivity)
                    binding.itemCount.text = "Liczba produktów: ${filteredProducts.size}"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        spinnerExp.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val cat =
                        if (spinnerCategory.selectedItemPosition == 0) null else spinnerCategory.selectedItem as String?
                    val filteredProducts = filterProducts(
                        products,
                        cat,
                        if(spinnerExp.selectedItemPosition == 0) null else spinnerExp.selectedItemPosition == 1
                    )
                    viewAdapter.notifyDataSetChanged()
                    recyclerView.adapter =
                        ProductAdapter(filteredProducts, supportFragmentManager, this@MainActivity)
                    binding.itemCount.text = "Liczba produktów: ${filteredProducts.size}"
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    private fun setupRecyclerView() {
        viewAdapter = ProductAdapter(products, supportFragmentManager, this)
        recyclerView = binding.recyclerView

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = viewAdapter
    }

    private fun sortProducts() {
        products = products.sortedBy {
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            formatter.isLenient = false
            formatter.parse(it.expirationDate)
        }
    }

    private fun checkExpirationDate() {
        val currentDate = Date()
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        formatter.isLenient = false
        products.forEach {
            val expirationDate = formatter.parse(it.expirationDate)
            if (expirationDate.before(currentDate)) {
                it.state = "Przeterminowany"
            } else {
                it.state = "Ważny"
            }
        }
    }

    private fun buildDataBase() =
        Room.databaseBuilder(applicationContext, DataBase::class.java, "pjatkapp-database7")
            .fallbackToDestructiveMigration().build()

    private fun populateDB() {
        dbExecutor.execute() {
            if (database.productDao().getAllProducts().isEmpty()) {
                var products = arrayOf(
                    Product(

                        "Produkt spożywczy",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Mleko",
                        "2024-06-15",
                        "2 litry",
                        false
                    ),
                    Product(

                        "Lek",
                        "Przeterminowany",
                        "url_zdjęcia".toByteArray(),
                        "Paracetamol",
                        "2023-05-01",
                        "1 opakowanie",
                        true
                    ),
                    Product(

                        "Kosmetyk",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Szampon do włosów",
                        "2024-12-07",
                        "1 szt",
                        false
                    ),
                    Product(

                        "Produkt spożywczy",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Jogurt",
                        "2024-11-11",
                        "200ml",
                        false
                    ),
                    Product(

                        "Produkt spożywczy",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Ser",
                        "2024-12-12",
                        "100 gram",
                        false
                    ),
                    Product(

                        "Produkt spożywczy",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Masło",
                        "2024-12-11",
                        "150 gram",
                        false
                    ),
                    Product(

                        "Lek",
                        "Przeterminowany",
                        "url_zdjęcia".toByteArray(),
                        "Ibuprofen",
                        "2023-05-01",
                        "1 opakowanie",
                        true
                    ),
                    Product(

                        "Lek",
                        "Przeterminowany",
                        "url_zdjęcia".toByteArray(),
                        "Witamina C",
                        "2023-05-01",
                        "1 opakowanie",
                        true
                    ),
                    Product(

                        "Kosmetyk",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Pasta do zębów",
                        "2028-03-07",
                        "25 ml",
                        false
                    ),
                    Product(

                        "Kosmetyk",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Krem do rąk",
                        "2028-03-07",
                        "100 ml",
                        false
                    ),
                    Product(

                        "Kosmetyk",
                        "Ważny",
                        "url_zdjęcia".toByteArray(),
                        "Perfumy",
                        "2028-03-07",
                        "100 ml",
                        false
                    ),

                    )
                products.forEach { product ->
                    database.productDao().insertProduct(product)
                }
            }
        }
    }
    @Insert
    fun insertProduct(product: Product) {
        dbExecutor.execute {
            database.productDao().insertProduct(product)
            products = database.productDao().getAllProducts()
            runOnUiThread {
                updateUI()
            }
        }
    }

    fun deleteProduct(product: Product) {
        dbExecutor.execute {
            database.productDao().deleteProduct(product)
            runOnUiThread {
                updateUI()
            }
        }
    }

    fun updateProduct(product: Product) {
        dbExecutor.execute {
            database.productDao().updateProduct(product)
            products = database.productDao().getAllProducts()
            runOnUiThread {
                updateUI()
            }
        }
    }

    fun filterProducts(
        products: List<Product>,
        category: String?,
        expired: Boolean?
    ): List<Product> {
        var filteredProducts = products

        if (category != null) {
            filteredProducts = filteredProducts.filter { it.category == category }
        }

        if (expired != null) {
            val currentDate = Date()
            val formatter = SimpleDateFormat("yyyy-MM-dd")
            formatter.isLenient = false
            filteredProducts = filteredProducts.filter {
                val expirationDate = formatter.parse(it.expirationDate)
                if (expired) {
                    expirationDate.before(currentDate)
                } else {
                    expirationDate.after(currentDate)
                }
            }
        }

        return filteredProducts
    }

}






