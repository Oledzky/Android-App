import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.pjatkapp.MainActivity
import com.example.pjatkapp.databinding.ItemProductBinding
import com.example.pjatkapp.Product
import com.example.pjatkapp.ProductInfo

 class ProductAdapter(private val products: List<Product>, private val fragmentManager: FragmentManager, private val mainActivity: MainActivity) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.bind(product)

        holder.itemView.setOnClickListener {
            if (product.state == "Ważny") {
                val bundle = Bundle().apply {
                    putInt("productId", product.id)
                    putString("productName", product.name)
                    putString("productExpirationDate", product.expirationDate)
                    putString("productQuantity", product.quantity ?: "")
                    putBoolean("productIsDisposed", product.isDisposed)
                    putString("productCategory", product.category)
                    putString("productState", product.state)
                    putString("productPhoto", product.photo.toString())
                }

                ProductInfo().apply {
                    arguments = bundle
                }.show(fragmentManager, "dialog")
                Toast.makeText(
                    holder.itemView.context,
                    "Kliknięto na produkt: ${product.name}",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Produkt '${product.name}' jest przeterminowany i nie można go edytować.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
            holder.itemView.setOnLongClickListener(View.OnLongClickListener {
                if (product.state == "Ważny") {

                    Toast.makeText(
                        holder.itemView.context,
                        "Dłużej przytrzymano: ${product.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showActionConfirmationDialog(holder.itemView.context, product)
                } else {
                    Toast.makeText(
                        holder.itemView.context,
                        "Dłużej przytrzymano przeterminowany: ${product.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showActionConfirmationDialog(holder.itemView.context, product)
                }
                true
            })

    }

        override fun getItemCount() = products.size

        class ProductViewHolder(private val binding: ItemProductBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun bind(product: Product) {
                binding.textViewName.text = product.name
                binding.textViewExpirationDate.text = product.expirationDate
                binding.textViewQuantity.text = product.quantity.toString()
                binding.textViewIsDisposed.text = if (product.isDisposed) "Wyrzucony" else "Przechowywany"
                binding.textViewCategory.text = product.category
                binding.textViewState.text = product.state
                binding.imageView.setImageBitmap(product.photo?.size?.let {
                    BitmapFactory.decodeByteArray(product.photo, 0, it)
                })

            }
        }
     private fun showActionConfirmationDialog(context: Context, product: Product) {
         AlertDialog.Builder(context).apply {
             setTitle("Potwierdzenie Akcji")
             setMessage("Czy na pewno chcesz wyrzucić, albo usunąć produkt?")
             if(product.isDisposed){
                 setPositiveButton("Usuń") { dialog, which ->
                     mainActivity.deleteProduct(product)
                     Toast.makeText(context, "Usunięto produkt: ${product.name}", Toast.LENGTH_SHORT).show()
                 }
                 setNegativeButton("Anuluj", null)
             } else {
                 setNeutralButton("Wyrzuć") { dialog, which ->
                     mainActivity.updateProduct(
                         product
                             .apply { isDisposed = true })
                     Toast.makeText(
                         context,
                         "Wyrzucono produkt: ${product.name}",
                         Toast.LENGTH_SHORT
                     ).show()
                 }
                 setPositiveButton("Usuń") { dialog, which ->
                     mainActivity.deleteProduct(product)
                     Toast.makeText(
                         context,
                         "Usunięto produkt: ${product.name}",
                         Toast.LENGTH_SHORT
                     ).show()
                 }
                 setNegativeButton("Anuluj", null)
             }
         }.show()
     }

    }


